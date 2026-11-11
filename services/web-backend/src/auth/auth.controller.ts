import { Controller, Post, Body, HttpCode, HttpStatus } from '@nestjs/common';
import { AuthService, UserSession } from './auth.service';
import { Public } from './auth.guard';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';

@ApiTags('auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Public()
  @Post('login')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Authenticate user with memory-hard KDF and issue signed JWT session' })
  @ApiResponse({ status: 200, description: 'Authentication successful' })
  @ApiResponse({ status: 401, description: 'Invalid credentials' })
  login(@Body() body: { email: string; password: string }): UserSession {
    return this.authService.login(body);
  }
}
