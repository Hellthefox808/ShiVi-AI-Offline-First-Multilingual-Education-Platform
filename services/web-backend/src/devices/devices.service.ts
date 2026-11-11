import { Injectable, NotFoundException, Optional } from '@nestjs/common';
import { HardwareState } from '@bhashasetu/contracts';
import { PersistenceService } from '../common/persistence.service';

export interface TabletDevice {
  deviceId: string;
  schoolId: string;
  assignedTeacherId: string;
  deviceModel: string;
  serialNumber: string;
  osVersion: string;
  totalRamMb: number;
  availableStorageMb: number;
  batteryPercentage: number;
  status: 'ACTIVE' | 'REVOKED' | 'MAINTENANCE_REQUIRED';
  lastSyncTimestamp: string;
  installedPackageVersion: string;
  offlineSyncBacklogCount: number;
}

@Injectable()
export class DevicesService {
  constructor(@Optional() private readonly persistence?: PersistenceService) {}

  private readonly COLLECTION = 'devices';

  private readonly seedDevices: TabletDevice[] = [
    {
      deviceId: 'TAB-DUMKA-042-01',
      schoolId: 'SCH-DUMKA-042',
      assignedTeacherId: 'USR-001',
      deviceModel: 'Lava T81N 4G Tablet (ARM64)',
      serialNumber: 'SN-LAVA-2026-9941',
      osVersion: 'Android 11 (Go Edition)',
      totalRamMb: 2048,
      availableStorageMb: 8420,
      batteryPercentage: 86,
      status: 'ACTIVE',
      lastSyncTimestamp: '2026-08-28T06:30:00.000Z',
      installedPackageVersion: '3.0.0-PROD',
      offlineSyncBacklogCount: 0
    },
    {
      deviceId: 'TAB-CHAIBASA-108-03',
      schoolId: 'SCH-CHAIBASA-108',
      assignedTeacherId: 'USR-003',
      deviceModel: 'Samsung Galaxy Tab A7 Lite',
      serialNumber: 'SN-SAM-2026-1182',
      osVersion: 'Android 12',
      totalRamMb: 3072,
      availableStorageMb: 14200,
      batteryPercentage: 92,
      status: 'ACTIVE',
      lastSyncTimestamp: '2026-08-28T05:45:00.000Z',
      installedPackageVersion: '3.0.0-PROD',
      offlineSyncBacklogCount: 3
    }
  ];

  private memoryFallback: TabletDevice[] | null = null;

  private get devices(): TabletDevice[] {
    if (!this.persistence) {
      if (!this.memoryFallback) this.memoryFallback = [...this.seedDevices];
      return this.memoryFallback;
    }
    return this.persistence.read<TabletDevice[]>(this.COLLECTION, [...this.seedDevices]);
  }

  private persist(): void {
    if (this.persistence) {
      this.persistence.write(this.COLLECTION, this.devices);
    }
  }

  findAll(): TabletDevice[] {
    return this.devices;
  }

  findOne(deviceId: string): TabletDevice {
    const device = this.devices.find((d) => d.deviceId === deviceId);
    if (!device) {
      throw new NotFoundException(`Device ${deviceId} not found in registry`);
    }
    return device;
  }

  registerOrHeartbeat(payload: Partial<TabletDevice>): TabletDevice {
    const existing = this.devices.find((d) => d.deviceId === payload.deviceId);
    if (existing) {
      Object.assign(existing, payload, { lastSyncTimestamp: new Date().toISOString() });
      this.persist();
      return existing;
    }
    const newDevice: TabletDevice = {
      deviceId: payload.deviceId || `TAB-${Date.now().toString().slice(-6)}`,
      schoolId: payload.schoolId || 'SCH-DUMKA-042',
      assignedTeacherId: payload.assignedTeacherId || 'USR-001',
      deviceModel: payload.deviceModel || 'Android Low-Cost Tablet (2GB)',
      serialNumber: payload.serialNumber || `SN-${Date.now()}`,
      osVersion: payload.osVersion || 'Android 10+',
      totalRamMb: payload.totalRamMb || 2048,
      availableStorageMb: payload.availableStorageMb || 8000,
      batteryPercentage: payload.batteryPercentage || 100,
      status: 'ACTIVE',
      lastSyncTimestamp: new Date().toISOString(),
      installedPackageVersion: '3.0.0-PROD',
      offlineSyncBacklogCount: 0
    };
    this.devices.push(newDevice);
    this.persist();
    return newDevice;
  }

  revoke(deviceId: string): TabletDevice {
    const device = this.findOne(deviceId);
    device.status = 'REVOKED';
    this.persist();
    return device;
  }
}
