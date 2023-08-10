/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { HardwareResources } from './HardwareResources';
import type { ShipmentHealth } from './ShipmentHealth';

export type Shipment = {
    id?: string;
    name: string;
    image: string;
    hardwareRequirements?: HardwareResources;
    shipId?: string;
    health?: ShipmentHealth;
};

