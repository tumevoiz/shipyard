/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { LighthouseError } from './LighthouseError';
import type { ResourceNotFoundError } from './ResourceNotFoundError';

export type ShipyardError = (LighthouseError | ResourceNotFoundError);

