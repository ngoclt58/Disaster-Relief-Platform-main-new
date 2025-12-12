interface VoiceCommand {
  id: string;
  command: string;
  action: (params?: any) => void;
  parameters?: string[];
}

interface SpeechRecognitionResult {
  transcript: string;
  confidence: number;
}

class VoiceCommandService {
  private recognition: any = null;
  private isListening = false;
  private commands: Map<string, VoiceCommand> = new Map();
  private supported = false;

  constructor() {
    this.initializeRecognition();
    this.registerDefaultCommands();
  }

  private initializeRecognition() {
    // Check for Web Speech API support
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    
    if (SpeechRecognition) {
      this.recognition = new SpeechRecognition();
      this.recognition.continuous = true;
      this.recognition.interimResults = false;
      this.recognition.lang = 'en-US';

      this.recognition.onresult = (event: any) => {
        const last = event.results.length - 1;
        const transcript = event.results[last][0].transcript;
        const confidence = event.results[last][0].confidence;

        this.onSpeechResult({ transcript, confidence });
      };

      this.recognition.onerror = (event: any) => {
        console.error('Speech recognition error:', event.error);
        this.onError(event.error);
      };

      this.recognition.onend = () => {
        if (this.isListening) {
          // Restart recognition if still listening
          this.recognition.start();
        }
      };

      this.supported = true;
    }
  }

  private registerDefaultCommands() {
    // Navigation commands
    this.registerCommand({
      id: 'navigate',
      command: 'navigate to',
      parameters: ['destination'],
      action: (params) => {
        window.dispatchEvent(new CustomEvent('voice-command-navigate', {
          detail: { destination: params.destination }
        }));
      }
    });

    this.registerCommand({
      id: 'go_back',
      command: 'go back',
      action: () => {
        window.history.back();
      }
    });

    this.registerCommand({
      id: 'open_dashboard',
      command: 'open dashboard',
      action: () => {
        window.location.href = '/dashboard';
      }
    });

    // Task commands
    this.registerCommand({
      id: 'create_task',
      command: 'create task',
      parameters: ['description'],
      action: (params) => {
        window.dispatchEvent(new CustomEvent('voice-command-create-task', {
          detail: { description: params.description }
        }));
      }
    });

    this.registerCommand({
      id: 'complete_task',
      command: 'complete task',
      parameters: ['task'],
      action: (params) => {
        window.dispatchEvent(new CustomEvent('voice-command-complete-task', {
          detail: { task: params.task }
        }));
      }
    });

    // Needs commands
    this.registerCommand({
      id: 'report_need',
      command: 'report need',
      parameters: ['item', 'quantity', 'priority'],
      action: (params) => {
        window.dispatchEvent(new CustomEvent('voice-command-report-need', {
          detail: params
        }));
      }
    });

    // Emergency commands
    this.registerCommand({
      id: 'emergency',
      command: 'emergency',
      action: () => {
        window.dispatchEvent(new CustomEvent('voice-command-emergency'));
      }
    });

    this.registerCommand({
      id: 'call_for_help',
      command: 'call for help',
      action: () => {
        window.dispatchEvent(new CustomEvent('voice-command-help'));
      }
    });

    // General commands
    this.registerCommand({
      id: 'stop_listening',
      command: 'stop listening',
      action: () => {
        this.stopListening();
      }
    });

    this.registerCommand({
      id: 'what_can_i_say',
      command: 'what can I say',
      action: () => {
        window.dispatchEvent(new CustomEvent('voice-command-help', {
          detail: { showCommands: true }
        }));
      }
    });
  }

  registerCommand(command: VoiceCommand) {
    this.commands.set(command.id, command);
  }

  async startListening(): Promise<boolean> {
    if (!this.supported) {
      throw new Error('Speech recognition is not supported on this device');
    }

    if (this.isListening) {
      return true;
    }

    try {
      await this.requestPermission();
      this.recognition.start();
      this.isListening = true;
      
      window.dispatchEvent(new CustomEvent('voice-listening-started'));
      return true;
    } catch (error) {
      console.error('Failed to start voice recognition:', error);
      return false;
    }
  }

  stopListening() {
    if (this.recognition && this.isListening) {
      this.recognition.stop();
      this.isListening = false;
      
      window.dispatchEvent(new CustomEvent('voice-listening-stopped'));
    }
  }

  private async requestPermission(): Promise<void> {
    // Request microphone permission
    if ('permissions' in navigator) {
      const result = await navigator.permissions.query({ name: 'microphone' as PermissionName });
      if (result.state === 'denied') {
        throw new Error('Microphone permission denied');
      }
    }
  }

  private onSpeechResult(result: SpeechRecognitionResult) {
    console.log('Speech result:', result);
    
    const command = this.parseCommand(result.transcript);
    if (command) {
      this.executeCommand(command, result);
    } else {
      window.dispatchEvent(new CustomEvent('voice-command-unknown', {
        detail: { transcript: result.transcript }
      }));
    }
  }

  private parseCommand(transcript: string): VoiceCommand | null {
    // Normalize transcript
    const normalizedTranscript = transcript.toLowerCase().trim();

    for (const command of this.commands.values()) {
      const normalizedCommand = command.command.toLowerCase();
      
      if (normalizedTranscript.includes(normalizedCommand)) {
        return command;
      }

      // Check for partial matches
      if (normalizedTranscript.split(' ').some(word => 
        normalizedCommand.split(' ').includes(word))) {
        return command;
      }
    }

    return null;
  }

  private executeCommand(command: VoiceCommand, result: SpeechRecognitionResult) {
    if (!command.parameters || command.parameters.length === 0) {
      command.action();
    } else {
      const params = this.extractParameters(result.transcript, command);
      command.action(params);
    }

    window.dispatchEvent(new CustomEvent('voice-command-executed', {
      detail: { command: command.id, transcript: result.transcript }
    }));
  }

  private extractParameters(transcript: string, command: VoiceCommand): Record<string, any> {
    const params: Record<string, any> = {};
    const words = transcript.toLowerCase().split(' ');

    if (command.parameters) {
      // Simple parameter extraction (can be enhanced with NLP)
      command.parameters.forEach(param => {
        const paramIndex = transcript.toLowerCase().indexOf(param.toLowerCase());
        if (paramIndex !== -1) {
          // Extract parameter value (simplified)
          const match = words[words.indexOf(param.toLowerCase()) + 1];
          if (match) {
            params[param] = match;
          }
        }
      });
    }

    return params;
  }

  private onError(error: string) {
    window.dispatchEvent(new CustomEvent('voice-error', {
      detail: { error }
    }));
  }

  getIsListening(): boolean {
    return this.isListening;
  }

  getSupported(): boolean {
    return this.supported;
  }

  getAvailableCommands(): VoiceCommand[] {
    return Array.from(this.commands.values());
  }

  async testMicrophone(): Promise<boolean> {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      stream.getTracks().forEach(track => track.stop());
      return true;
    } catch (error) {
      return false;
    }
  }
}

export const voiceCommandService = new VoiceCommandService();
export default voiceCommandService;

