import {ShipyardError} from "../generated";

export const isAPIError = <T>(obj: T | ShipyardError): obj is ShipyardError => {
  return (<ShipyardError>obj).kind !== undefined;
}