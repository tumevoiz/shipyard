/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ApplicationRefState } from '../models/ApplicationRefState';
import type { ClusterStats } from '../models/ClusterStats';
import type { HardwareResources } from '../models/HardwareResources';
import type { Ship } from '../models/Ship';
import type { Shipment } from '../models/Shipment';
import type { ShipmentHealth } from '../models/ShipmentHealth';
import type { ShipyardError } from '../models/ShipyardError';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class DefaultService {

    /**
     * @returns ClusterStats
     * @returns ShipyardError
     * @throws ApiError
     */
    public static getApiLighthouseClusterStats(): CancelablePromise<ClusterStats | ShipyardError> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/lighthouse/cluster-stats',
        });
    }

    /**
     * @returns ApplicationRefState
     * @returns ShipyardError
     * @throws ApiError
     */
    public static getApiLighthouseState(): CancelablePromise<ApplicationRefState | ShipyardError> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/lighthouse/state',
        });
    }

    /**
     * @returns Ship
     * @returns ShipyardError
     * @throws ApiError
     */
    public static getApiShips(): CancelablePromise<Array<Ship> | ShipyardError> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/ships',
        });
    }

    /**
     * @param requestBody
     * @returns any
     * @returns ShipyardError
     * @throws ApiError
     */
    public static postApiShips(
        requestBody: Ship,
    ): CancelablePromise<any | ShipyardError> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/ships',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Invalid value for: body`,
            },
        });
    }

    /**
     * @param shipId
     * @returns Ship
     * @returns ShipyardError
     * @throws ApiError
     */
    public static getApiShipsShipid(
        shipId: string,
    ): CancelablePromise<Ship | ShipyardError> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/ships/{shipId}',
            path: {
                'shipId': shipId,
            },
            errors: {
                400: `Invalid value for: path parameter shipId`,
            },
        });
    }

    /**
     * @param name
     * @returns Ship
     * @returns ShipyardError
     * @throws ApiError
     */
    public static getApiShipsName(
        name: string,
    ): CancelablePromise<Ship | ShipyardError> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/ships/{name}',
            path: {
                'name': name,
            },
        });
    }

    /**
     * @param p1
     * @param requestBody
     * @returns any
     * @returns ShipyardError
     * @throws ApiError
     */
    public static putApiShipsP1Resources(
        p1: string,
        requestBody: HardwareResources,
    ): CancelablePromise<any | ShipyardError> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/ships/{p1}/resources',
            path: {
                'p1': p1,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Invalid value for: path parameter p1, Invalid value for: body`,
            },
        });
    }

    /**
     * Find all shipments
     * @returns Shipment
     * @returns ShipyardError
     * @throws ApiError
     */
    public static getApiShipments(): CancelablePromise<Array<Shipment> | ShipyardError> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/shipments',
        });
    }

    /**
     * Schedule a new shipment
     * @param requestBody
     * @returns any
     * @returns ShipyardError
     * @throws ApiError
     */
    public static postApiShipments(
        requestBody: Shipment,
    ): CancelablePromise<any | ShipyardError> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/shipments',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Invalid value for: body`,
            },
        });
    }

    /**
     * Find all shipments by ship ID
     * @param shipId
     * @returns Shipment
     * @returns ShipyardError
     * @throws ApiError
     */
    public static getApiShipmentsShipid(
        shipId: string,
    ): CancelablePromise<Array<Shipment> | ShipyardError> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/shipments/{shipId}',
            path: {
                'shipId': shipId,
            },
            errors: {
                400: `Invalid value for: path parameter shipId`,
            },
        });
    }

    /**
     * Delete shipment by ID
     * @param shipmentId
     * @returns any
     * @returns ShipyardError
     * @throws ApiError
     */
    public static deleteApiShipmentsShipmentid(
        shipmentId: string,
    ): CancelablePromise<any | ShipyardError> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/api/shipments/{shipmentId}',
            path: {
                'shipmentId': shipmentId,
            },
            errors: {
                400: `Invalid value for: path parameter shipmentId`,
            },
        });
    }

    /**
     * Update the shipment status
     * @param shipmentId
     * @param requestBody
     * @returns any
     * @returns ShipyardError
     * @throws ApiError
     */
    public static putApiShipmentsShipmentidHealth(
        shipmentId: string,
        requestBody: ShipmentHealth,
    ): CancelablePromise<any | ShipyardError> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/shipments/{shipmentId}/health',
            path: {
                'shipmentId': shipmentId,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Invalid value for: path parameter shipmentId, Invalid value for: body`,
            },
        });
    }

}
