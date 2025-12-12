interface BiometricAuthResult {
  success: boolean;
  method?: 'fingerprint' | 'face' | 'voice';
  error?: string;
}

class BiometricAuthService {
  private publicKey: any = null;

  async registerBiometric(userId: string): Promise<boolean> {
    if (!this.isSupported()) {
      throw new Error('Biometric authentication is not supported on this device');
    }

    try {
      // Create credential options
      const publicKeyCredentialCreationOptions: PublicKeyCredentialCreationOptions = {
        challenge: new Uint8Array(32),
        rp: {
          name: 'Disaster Relief Platform',
          id: window.location.hostname
        },
        user: {
          id: new TextEncoder().encode(userId),
          name: userId,
          displayName: userId
        },
        pubKeyCredParams: [
          { alg: -7, type: 'public-key' }, // ES256
          { alg: -257, type: 'public-key' } // RS256
        ],
        authenticatorSelection: {
          authenticatorAttachment: 'platform', // Platform authenticator (fingerprint/face)
          requireResidentKey: false,
          userVerification: 'required'
        },
        timeout: 60000,
        attestation: 'direct'
      };

      // Register biometric
      const credential = await navigator.credentials.create({
        publicKey: publicKeyCredentialCreationOptions
      }) as PublicKeyCredential;

      if (credential && credential.response) {
        const publicKey = this.extractPublicKey(credential);
        this.publicKey = publicKey;
        return true;
      }

      return false;
    } catch (error: any) {
      console.error('Biometric registration failed:', error);
      throw error;
    }
  }

  async authenticate(): Promise<BiometricAuthResult> {
    if (!this.isSupported()) {
      return { success: false, error: 'Biometric authentication not supported' };
    }

    try {
      const publicKeyCredentialRequestOptions: PublicKeyCredentialRequestOptions = {
        challenge: new Uint8Array(32),
        allowCredentials: [],
        timeout: 60000,
        userVerification: 'required'
      };

      const assertion = await navigator.credentials.get({
        publicKey: publicKeyCredentialRequestOptions
      }) as PublicKeyCredential;

      if (assertion && assertion.response) {
        return {
          success: true,
          method: this.detectMethod(assertion)
        };
      }

      return { success: false, error: 'Authentication failed' };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }

  async enableFingerprint(): Promise<boolean> {
    return this.registerBiometric('fingerprint_user');
  }

  async enableFaceRecognition(): Promise<boolean> {
    return this.registerBiometric('face_user');
  }

  async enableVoiceRecognition(): Promise<boolean> {
    // Voice recognition using Web Speech API
    if ('speechSynthesis' in window) {
      return true;
    }
    return false;
  }

  private extractPublicKey(credential: PublicKeyCredential): any {
    const response = credential.response as AuthenticatorAttestationResponse;
    return {
      id: credential.id,
      rawId: Array.from(new Uint8Array(credential.rawId)),
      response: {
        attestationObject: Array.from(new Uint8Array(response.attestationObject)),
        clientDataJSON: Array.from(new Uint8Array(response.clientDataJSON))
      }
    };
  }

  private detectMethod(credential: PublicKeyCredential): 'fingerprint' | 'face' | 'voice' {
    // Try to detect the authentication method used
    // This is simplified - actual detection would use authenticator data
    if ('authenticatorData' in (credential.response as any)) {
      return 'fingerprint';
    }
    return 'face';
  }

  isSupported(): boolean {
    // Check for WebAuthn support
    return 'PublicKeyCredential' in window && 
           'credentials' in navigator &&
           typeof (navigator.credentials as any).create === 'function';
  }

  getAvailableMethods(): Array<'fingerprint' | 'face' | 'voice'> {
    const methods: Array<'fingerprint' | 'face' | 'voice'> = [];

    if (this.isSupported()) {
      methods.push('fingerprint', 'face');
    }

    if ('speechSynthesis' in window) {
      methods.push('voice');
    }

    return methods;
  }

  async checkBiometricStatus(): Promise<boolean> {
    // Check if biometrics are registered and available
    try {
      const result = await this.authenticate();
      return result.success;
    } catch {
      return false;
    }
  }

  async testBiometric(): Promise<BiometricAuthResult> {
    return this.authenticate();
  }

  async removeBiometric(): Promise<void> {
    this.publicKey = null;
    // Clear stored credentials
    if ('credentials' in navigator) {
      // Note: Actual credential removal would require server-side support
      console.log('Biometric credentials cleared locally');
    }
  }
}

export const biometricAuthService = new BiometricAuthService();
export default biometricAuthService;



