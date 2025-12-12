/**
 * Video Conferencing Service
 * Handles WebRTC video calls and remote coordination
 */

import { apiService } from './api';

export interface VideoConference {
  id: string;
  title: string;
  description: string;
  type: 'EMERGENCY' | 'COORDINATION' | 'TRAINING' | 'MEETING';
  status: 'SCHEDULED' | 'ACTIVE' | 'ENDED' | 'CANCELLED';
  organizerId: string;
  organizerName: string;
  createdAt: string;
  startedAt?: string;
  endedAt?: string;
  maxParticipants: number;
  durationMinutes: number;
  roomId: string;
  meetingUrl: string;
  passcode: string;
}

export interface ConferenceParticipant {
  userId: string;
  userName: string;
  userEmail: string;
  role: 'HOST' | 'PARTICIPANT' | 'OBSERVER';
  joinedAt: string;
  leftAt?: string;
  audioEnabled: boolean;
  videoEnabled: boolean;
  screenSharing: boolean;
}

export interface ConferenceJoinResult {
  conference: VideoConference;
  participant: ConferenceParticipant;
  webRTCConfig: WebRTCConfig;
  iceServers: IceServer[];
}

export interface WebRTCConfig {
  roomId: string;
  conferenceId: string;
  maxParticipants: number;
  audioEnabled: boolean;
  videoEnabled: boolean;
  screenShareEnabled: boolean;
  chatEnabled: boolean;
  recordingEnabled: boolean;
}

export interface IceServer {
  url: string;
  username?: string;
  credential?: string;
}

export interface ConferenceSettings {
  title?: string;
  description?: string;
  maxParticipants?: number;
  durationMinutes?: number;
}

class VideoConferencingService {
  private localStream: MediaStream | null = null;
  private peerConnections: Map<string, RTCPeerConnection> = new Map();
  private dataChannel: RTCDataChannel | null = null;

  /**
   * Create a new video conference
   */
  async createConference(title: string, description: string, type: string): Promise<VideoConference> {
    const response = await apiService.post('/video-conference/create', {
      title,
      description,
      type
    });
    return response;
  }

  /**
   * Join a video conference
   */
  async joinConference(conferenceId: string): Promise<ConferenceJoinResult> {
    const response = await apiService.post(`/video-conference/${conferenceId}/join`);
    return response;
  }

  /**
   * Leave a video conference
   */
  async leaveConference(conferenceId: string): Promise<void> {
    await apiService.post(`/video-conference/${conferenceId}/leave`);
    this.cleanup();
  }

  /**
   * Get user's conferences
   */
  async getUserConferences(): Promise<VideoConference[]> {
    const response = await apiService.get('/video-conference/my-conferences');
    return response;
  }

  /**
   * Get conference participants
   */
  async getConferenceParticipants(conferenceId: string): Promise<ConferenceParticipant[]> {
    const response = await apiService.get(`/video-conference/${conferenceId}/participants`);
    return response;
  }

  /**
   * Update conference settings
   */
  async updateConferenceSettings(conferenceId: string, settings: ConferenceSettings): Promise<VideoConference> {
    const response = await apiService.put(`/video-conference/${conferenceId}/settings`, settings);
    return response;
  }

  /**
   * End a conference
   */
  async endConference(conferenceId: string): Promise<void> {
    await apiService.post(`/video-conference/${conferenceId}/end`);
  }

  /**
   * Initialize WebRTC for video conferencing
   */
  async initializeWebRTC(conferenceId: string, iceServers: IceServer[]): Promise<void> {
    try {
      // Get user media
      this.localStream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true
      });

      // Create peer connection
      const peerConnection = new RTCPeerConnection({
        iceServers: iceServers.map(server => ({
          urls: server.url,
          username: server.username,
          credential: server.credential
        }))
      });

      // Add local stream to peer connection
      this.localStream.getTracks().forEach(track => {
        peerConnection.addTrack(track, this.localStream!);
      });

      // Handle incoming streams
      peerConnection.ontrack = (event) => {
        this.handleRemoteStream(event.streams[0]);
      };

      // Handle ICE candidates
      peerConnection.onicecandidate = (event) => {
        if (event.candidate) {
          this.sendIceCandidate(conferenceId, event.candidate);
        }
      };

      // Create data channel for chat
      this.dataChannel = peerConnection.createDataChannel('chat');
      this.dataChannel.onopen = () => {
        console.log('Data channel opened');
      };

      this.peerConnections.set(conferenceId, peerConnection);

    } catch (error) {
      console.error('Error initializing WebRTC:', error);
      throw error;
    }
  }

  /**
   * Start screen sharing
   */
  async startScreenShare(): Promise<void> {
    try {
      const screenStream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: true
      });

      // Replace video track
      const videoTrack = screenStream.getVideoTracks()[0];
      const sender = this.getVideoSender();
      if (sender) {
        await sender.replaceTrack(videoTrack);
      }

      // Handle screen share end
      videoTrack.onended = () => {
        this.stopScreenShare();
      };

    } catch (error) {
      console.error('Error starting screen share:', error);
      throw error;
    }
  }

  /**
   * Stop screen sharing
   */
  async stopScreenShare(): Promise<void> {
    try {
      const videoTrack = this.localStream?.getVideoTracks()[0];
      if (videoTrack) {
        videoTrack.stop();
      }
    } catch (error) {
      console.error('Error stopping screen share:', error);
    }
  }

  /**
   * Toggle audio
   */
  toggleAudio(): void {
    if (this.localStream) {
      const audioTrack = this.localStream.getAudioTracks()[0];
      if (audioTrack) {
        audioTrack.enabled = !audioTrack.enabled;
      }
    }
  }

  /**
   * Toggle video
   */
  toggleVideo(): void {
    if (this.localStream) {
      const videoTrack = this.localStream.getVideoTracks()[0];
      if (videoTrack) {
        videoTrack.enabled = !videoTrack.enabled;
      }
    }
  }

  /**
   * Send chat message
   */
  sendChatMessage(message: string): void {
    if (this.dataChannel && this.dataChannel.readyState === 'open') {
      this.dataChannel.send(JSON.stringify({
        type: 'chat',
        message,
        timestamp: new Date().toISOString()
      }));
    }
  }

  /**
   * Get local stream
   */
  getLocalStream(): MediaStream | null {
    return this.localStream;
  }

  /**
   * Cleanup resources
   */
  private cleanup(): void {
    if (this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop());
      this.localStream = null;
    }

    this.peerConnections.forEach(connection => {
      connection.close();
    });
    this.peerConnections.clear();

    if (this.dataChannel) {
      this.dataChannel.close();
      this.dataChannel = null;
    }
  }

  /**
   * Handle remote stream
   */
  private handleRemoteStream(stream: MediaStream): void {
    // Emit event for UI to handle
    const event = new CustomEvent('remoteStream', { detail: stream });
    window.dispatchEvent(event);
  }

  /**
   * Send ICE candidate
   */
  private async sendIceCandidate(conferenceId: string, candidate: RTCIceCandidate): Promise<void> {
    // In real implementation, send via WebSocket
    console.log('Sending ICE candidate:', candidate);
  }

  /**
   * Get video sender
   */
  private getVideoSender(): RTCRtpSender | null {
    for (const connection of this.peerConnections.values()) {
      const senders = connection.getSenders();
      const videoSender = senders.find(sender => 
        sender.track && sender.track.kind === 'video'
      );
      if (videoSender) {
        return videoSender;
      }
    }
    return null;
  }
}

export const videoConferencingService = new VideoConferencingService();


