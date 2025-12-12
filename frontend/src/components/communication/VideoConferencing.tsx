import React, { useState, useEffect, useRef } from 'react';
import { videoConferencingService, VideoConference, ConferenceJoinResult } from '../../services/videoConferencingService';

interface VideoConferencingProps {
  conferenceId?: string;
  onConferenceEnd?: () => void;
}

const VideoConferencing: React.FC<VideoConferencingProps> = ({ conferenceId, onConferenceEnd }) => {
  const [conference, setConference] = useState<VideoConference | null>(null);
  const [joinResult, setJoinResult] = useState<ConferenceJoinResult | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [isAudioEnabled, setIsAudioEnabled] = useState(true);
  const [isVideoEnabled, setIsVideoEnabled] = useState(true);
  const [isScreenSharing, setIsScreenSharing] = useState(false);
  const [chatMessage, setChatMessage] = useState('');
  const [chatMessages, setChatMessages] = useState<Array<{ id: string; message: string; timestamp: string; sender: string }>>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const localVideoRef = useRef<HTMLVideoElement>(null);
  const remoteVideoRef = useRef<HTMLVideoElement>(null);
  const chatContainerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (conferenceId) {
      joinConference();
    }

    // Listen for remote streams
    const handleRemoteStream = (event: CustomEvent) => {
      if (remoteVideoRef.current) {
        remoteVideoRef.current.srcObject = event.detail;
      }
    };

    window.addEventListener('remoteStream', handleRemoteStream as EventListener);

    return () => {
      window.removeEventListener('remoteStream', handleRemoteStream as EventListener);
      cleanup();
    };
  }, [conferenceId]);

  const joinConference = async () => {
    if (!conferenceId) return;

    setLoading(true);
    setError(null);

    try {
      const result = await videoConferencingService.joinConference(conferenceId);
      setJoinResult(result);
      setConference(result.conference);

      // Initialize WebRTC
      await videoConferencingService.initializeWebRTC(conferenceId, result.iceServers);
      
      // Get local stream
      const localStream = videoConferencingService.getLocalStream();
      if (localStream && localVideoRef.current) {
        localVideoRef.current.srcObject = localStream;
      }

      setIsConnected(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to join conference');
    } finally {
      setLoading(false);
    }
  };

  const leaveConference = async () => {
    if (!conferenceId) return;

    try {
      await videoConferencingService.leaveConference(conferenceId);
      setIsConnected(false);
      setConference(null);
      setJoinResult(null);
      onConferenceEnd?.();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to leave conference');
    }
  };

  const toggleAudio = () => {
    videoConferencingService.toggleAudio();
    setIsAudioEnabled(!isAudioEnabled);
  };

  const toggleVideo = () => {
    videoConferencingService.toggleVideo();
    setIsVideoEnabled(!isVideoEnabled);
  };

  const toggleScreenShare = async () => {
    try {
      if (isScreenSharing) {
        await videoConferencingService.stopScreenShare();
        setIsScreenSharing(false);
      } else {
        await videoConferencingService.startScreenShare();
        setIsScreenSharing(true);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to toggle screen share');
    }
  };

  const sendChatMessage = () => {
    if (!chatMessage.trim()) return;

    videoConferencingService.sendChatMessage(chatMessage);
    
    // Add to local chat messages
    const newMessage = {
      id: `msg_${Date.now()}`,
      message: chatMessage,
      timestamp: new Date().toISOString(),
      sender: 'You'
    };
    
    setChatMessages(prev => [...prev, newMessage]);
    setChatMessage('');

    // Scroll to bottom
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
    }
  };

  const cleanup = () => {
    if (conferenceId) {
      videoConferencingService.leaveConference(conferenceId);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        <span className="ml-3 text-gray-600">Joining conference...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <h3 className="text-sm font-medium text-red-800">Error</h3>
            <p className="mt-1 text-sm text-red-700">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  if (!isConnected || !conference) {
    return (
      <div className="text-center py-8">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Video Conference</h3>
        <p className="text-gray-600 mb-4">Click to join the conference</p>
        <button
          onClick={joinConference}
          className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
        >
          Join Conference
        </button>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-lg overflow-hidden">
      {/* Header */}
      <div className="bg-gray-800 text-white p-4">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-semibold">{conference.title}</h3>
            <p className="text-sm text-gray-300">{conference.description}</p>
          </div>
          <button
            onClick={leaveConference}
            className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
          >
            Leave
          </button>
        </div>
      </div>

      <div className="flex h-96">
        {/* Video Area */}
        <div className="flex-1 bg-gray-900 relative">
          {/* Remote Video */}
          <video
            ref={remoteVideoRef}
            autoPlay
            playsInline
            className="w-full h-full object-cover"
          />
          
          {/* Local Video */}
          <div className="absolute bottom-4 right-4 w-32 h-24 bg-gray-800 rounded-lg overflow-hidden">
            <video
              ref={localVideoRef}
              autoPlay
              playsInline
              muted
              className="w-full h-full object-cover"
            />
          </div>

          {/* Controls Overlay */}
          <div className="absolute bottom-4 left-4 flex space-x-2">
            <button
              onClick={toggleAudio}
              className={`p-3 rounded-full ${isAudioEnabled ? 'bg-gray-700 text-white' : 'bg-red-600 text-white'}`}
            >
              {isAudioEnabled ? (
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M9.383 3.076A1 1 0 0110 4v12a1 1 0 01-1.617.793L5.617 14H3a1 1 0 01-1-1V7a1 1 0 011-1h2.617l2.766-2.793a1 1 0 011.617.793zM14.657 2.929a1 1 0 011.414 0A9.972 9.972 0 0119 10a9.972 9.972 0 01-2.929 7.071 1 1 0 01-1.414-1.414A7.971 7.971 0 0017 10c0-2.21-.894-4.208-2.343-5.657a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              ) : (
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M9.383 3.076A1 1 0 0110 4v12a1 1 0 01-1.617.793L5.617 14H3a1 1 0 01-1-1V7a1 1 0 011-1h2.617l2.766-2.793a1 1 0 011.617.793zM12.293 7.293a1 1 0 011.414 0L15 8.586l1.293-1.293a1 1 0 111.414 1.414L16.414 10l1.293 1.293a1 1 0 01-1.414 1.414L15 11.414l-1.293 1.293a1 1 0 01-1.414-1.414L13.586 10l-1.293-1.293a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              )}
            </button>

            <button
              onClick={toggleVideo}
              className={`p-3 rounded-full ${isVideoEnabled ? 'bg-gray-700 text-white' : 'bg-red-600 text-white'}`}
            >
              {isVideoEnabled ? (
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M2 6a2 2 0 012-2h6a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2V6zM14.553 7.106A1 1 0 0014 8v4a1 1 0 00.553.894l2 1A1 1 0 0018 13V7a1 1 0 00-1.447-.894l-2 1z" />
                </svg>
              ) : (
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019 10c0-2.21-.894-4.208-2.343-5.657a1 1 0 00-1.414 1.414A7.971 7.971 0 0117 10c0 1.38-.56 2.63-1.464 3.536L3.707 2.293z" clipRule="evenodd" />
                </svg>
              )}
            </button>

            <button
              onClick={toggleScreenShare}
              className={`p-3 rounded-full ${isScreenSharing ? 'bg-blue-600 text-white' : 'bg-gray-700 text-white'}`}
            >
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 011 1v8a1 1 0 01-1 1H4a1 1 0 01-1-1V4zm2 1v6h10V5H5z" clipRule="evenodd" />
              </svg>
            </button>
          </div>
        </div>

        {/* Chat Area */}
        <div className="w-80 bg-white border-l border-gray-200 flex flex-col">
          <div className="p-4 border-b border-gray-200">
            <h4 className="font-semibold text-gray-900">Chat</h4>
          </div>
          
          <div
            ref={chatContainerRef}
            className="flex-1 p-4 overflow-y-auto space-y-2"
          >
            {chatMessages.map((msg) => (
              <div key={msg.id} className="flex flex-col">
                <div className="flex items-center space-x-2">
                  <span className="text-sm font-medium text-gray-900">{msg.sender}</span>
                  <span className="text-xs text-gray-500">{new Date(msg.timestamp).toLocaleTimeString()}</span>
                </div>
                <p className="text-sm text-gray-700">{msg.message}</p>
              </div>
            ))}
          </div>
          
          <div className="p-4 border-t border-gray-200">
            <div className="flex space-x-2">
              <input
                type="text"
                value={chatMessage}
                onChange={(e) => setChatMessage(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && sendChatMessage()}
                placeholder="Type a message..."
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <button
                onClick={sendChatMessage}
                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
              >
                Send
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VideoConferencing;


