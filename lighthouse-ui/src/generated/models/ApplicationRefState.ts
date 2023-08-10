/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { Ship } from './Ship';
import type { Shipment } from './Shipment';

export type ApplicationRefState = {
    ships?: Array<Ship>;
    shipments?: Array<Shipment>;
};

