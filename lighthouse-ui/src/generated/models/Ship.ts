/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { HardwareResources } from './HardwareResources';

/**
 * Dock is representation of computing node
 */
export type Ship = {
    id: string;
    name: string;
    ipAddress: string;
    shipmentCidr: string;
    lighthouse: boolean;
    currentHardwareResourcesUsage: HardwareResources;
};

