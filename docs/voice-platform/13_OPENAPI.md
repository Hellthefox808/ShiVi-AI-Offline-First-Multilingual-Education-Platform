# VoxBridge AI — OpenAPI 3.1.0 Specification

```yaml
openapi: 3.1.0
info:
  title: VoxBridge AI Voice Translation Platform API
  version: 1.0.0
  description: |
    Production-grade enterprise multilingual voice translation, transcription, 
    and neural speech synthesis platform. Supports real-time bidirectional streaming, 
    asynchronous batch processing, and multi-party meeting interpretation.
  contact:
    name: VoxBridge Developer Support
    email: api-support@voxbridge.ai
    url: https://docs.voxbridge.ai
  license:
    name: Proprietary Enterprise License
    url: https://voxbridge.ai/legal/terms

servers:
  - url: https://api.voxbridge.ai/v1
    description: Global Anycast Production API Cluster
  - url: https://api.sandbox.voxbridge.ai/v1
    description: Isolated Sandbox Environment (Synthetic Inference)

security:
  - ApiKeyAuth: []

paths:
  /sessions:
    post:
      summary: Initialize a Real-Time Streaming Session
      description: Creates a session and returns a short-lived token to connect to the WebSocket/WebRTC streaming gateway.
      operationId: createSession
      tags:
        - Realtime Sessions
      parameters:
        - $ref: '#/components/parameters/IdempotencyKey'
        - $ref: '#/components/parameters/VoxProjectId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateSessionRequest'
      responses:
        '201':
          description: Session successfully provisioned.
          headers:
            X-Request-Id:
              schema:
                type: string
            X-Trace-Id:
              schema:
                type: string
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CreateSessionResponse'
        '400':
          $ref: '#/components/responses/400BadRequest'
        '401':
          $ref: '#/components/responses/401Unauthorized'
        '429':
          $ref: '#/components/responses/429RateLimited'
        '500':
          $ref: '#/components/responses/500InternalError'

  /jobs:
    post:
      summary: Submit Asynchronous Batch Audio Translation Job
      description: Upload or link audio file for transcription, language translation, and synthesized audio generation.
      operationId: submitBatchJob
      tags:
        - Batch Processing
      parameters:
        - $ref: '#/components/parameters/IdempotencyKey'
        - $ref: '#/components/parameters/VoxProjectId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SubmitJobRequest'
      responses:
        '202':
          description: Job accepted and queued for worker execution.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/JobStatusResponse'
        '400':
          $ref: '#/components/responses/400BadRequest'
        '402':
          $ref: '#/components/responses/402PaymentRequired'
        '429':
          $ref: '#/components/responses/429RateLimited'

  /jobs/{job_id}:
    get:
      summary: Get Batch Job Status & Download Links
      operationId: getJobStatus
      tags:
        - Batch Processing
      parameters:
        - name: job_id
          in: path
          required: true
          schema:
            type: string
            pattern: '^job_[0-9A-HJ-KM-NP-TV-Z]{26}$'
      responses:
        '200':
          description: Current job execution status.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/JobStatusResponse'
        '404':
          $ref: '#/components/responses/404NotFound'

  /translations:
    post:
      summary: Synchronous Direct Text Translation
      description: Translate text with domain glossary injection and tone preservation.
      operationId: translateText
      tags:
        - Text Translation
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/TranslateTextRequest'
      responses:
        '200':
          description: Translated text payload.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TranslateTextResponse'

components:
  securitySchemes:
    ApiKeyAuth:
      type: http
      scheme: bearer
      bearerFormat: API_KEY
      description: Provide secret API key (`vox_live_*` or `vox_test_*`).

  parameters:
    IdempotencyKey:
      name: Idempotency-Key
      in: header
      required: false
      schema:
        type: string
        maxLength: 64
      description: UUID preventing duplicate processing on network retries.
    VoxProjectId:
      name: Vox-Project-Id
      in: header
      required: false
      schema:
        type: string
        pattern: '^proj_[0-9A-HJ-KM-NP-TV-Z]{26}$'
      description: Target project identifier.

  schemas:
    CreateSessionRequest:
      type: object
      required:
        - mode
        - source_language
        - target_languages
        - audio_format
      properties:
        mode:
          type: string
          enum:
            - SPEECH_TO_TEXT
            - SPEECH_TO_TRANSLATED_TEXT
            - REALTIME_TRANSLATED_SPEECH
            - TWO_WAY_INTERPRETATION
        source_language:
          type: string
          example: en-US
        target_languages:
          type: array
          items:
            type: string
          example: ["es-ES", "hi-IN"]
        audio_format:
          $ref: '#/components/schemas/AudioFormat'
        voice_config:
          $ref: '#/components/schemas/VoiceConfig'
        features:
          type: object
          properties:
            enable_vad:
              type: boolean
              default: true
            vad_threshold:
              type: number
              default: 0.55
            glossary_id:
              type: string

    AudioFormat:
      type: object
      required:
        - codec
        - sample_rate_hz
        - channels
      properties:
        codec:
          type: string
          enum: [OPUS, LINEAR16, FLAC, AAC, MP3]
        sample_rate_hz:
          type: integer
          enum: [8000, 16000, 24000, 44100, 48000]
        channels:
          type: integer
          enum: [1, 2]
        frame_duration_ms:
          type: integer
          enum: [10, 20, 40, 60]
          default: 20

    VoiceConfig:
      type: object
      properties:
        voice_id:
          type: string
          example: vox_voice_neural_mateo_es
        speed:
          type: number
          minimum: 0.5
          maximum: 2.0
          default: 1.0
        pitch:
          type: number
          minimum: -20.0
          maximum: 20.0
          default: 0.0

    CreateSessionResponse:
      type: object
      required:
        - session_id
        - connection_url
        - session_token
        - token_expires_at
      properties:
        session_id:
          type: string
        connection_url:
          type: string
          format: uri
        session_token:
          type: string
        token_expires_at:
          type: string
          format: date-time
        assigned_edge_region:
          type: string

    SubmitJobRequest:
      type: object
      required:
        - job_type
        - input
        - output
      properties:
        job_type:
          type: string
          enum: [BATCH_AUDIO_TRANSLATE, BATCH_TRANSCRIPTION_ONLY]
        input:
          type: object
          required:
            - source_type
            - url
          properties:
            source_type:
              type: string
              enum: [PRE_SIGNED_URL, S3_URI, DIRECT_UPLOAD]
            url:
              type: string
              format: uri
            source_language:
              type: string
              default: auto
        output:
          type: object
          required:
            - target_languages
          properties:
            target_languages:
              type: array
              items:
                type: string
            generate_speech:
              type: boolean
              default: false
            destination_s3_uri:
              type: string
        webhook_url:
          type: string
          format: uri

    JobStatusResponse:
      type: object
      required:
        - job_id
        - status
        - progress_percentage
      properties:
        job_id:
          type: string
        status:
          type: string
          enum: [QUEUED, PROCESSING, COMPLETED, FAILED, CANCELLED]
        progress_percentage:
          type: integer
          minimum: 0
          maximum: 100
        result_urls:
          type: object
          additionalProperties:
            type: string
            format: uri
        created_at:
          type: string
          format: date-time
        completed_at:
          type: string
          format: date-time

    TranslateTextRequest:
      type: object
      required:
        - text
        - source_language
        - target_language
      properties:
        text:
          type: string
          maxLength: 10000
        source_language:
          type: string
        target_language:
          type: string
        glossary_id:
          type: string

    TranslateTextResponse:
      type: object
      required:
        - translated_text
        - source_language
        - target_language
      properties:
        translated_text:
          type: string
        detected_source_language:
          type: string
        character_count:
          type: integer

    ErrorResponse:
      type: object
      required:
        - error
      properties:
        error:
          type: object
          required:
            - code
            - message
            - status
            - request_id
            - retryable
          properties:
            code:
              type: string
            message:
              type: string
            status:
              type: integer
            request_id:
              type: string
            trace_id:
              type: string
            retryable:
              type: boolean
            doc_url:
              type: string
              format: uri

  responses:
    400BadRequest:
      description: Malformed request body or unsupported audio parameters.
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    401Unauthorized:
      description: Missing, revoked, or invalid API key.
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    402PaymentRequired:
      description: Tenant balance exhausted or quota limit reached.
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    404NotFound:
      description: Requested session or job resource does not exist.
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    429RateLimited:
      description: Exceeded allowed requests per minute.
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    500InternalError:
      description: Unhandled platform failure.
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
```
