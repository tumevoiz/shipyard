import React, {useEffect, useState} from 'react';

import {DefaultService, Ship, ShipyardError} from "../generated";
import {isAPIError} from "../utils";

export const ShipListPage: React.FC = () => {
  const [shipData, setShipData] = useState<Array<Ship>>([])

  const shipPromise = async() => {
    const ships = await DefaultService.getApiShips()
    if(isAPIError(ships)) {
      console.error("API error!")
      return;
    }

    setShipData(ships)
  }


  useEffect(() => {
    shipPromise().catch(console.error)
    const interval = setInterval(() => {
      shipPromise().catch(console.error)
    }, 5000)
    return () => clearInterval(interval)
  }, [])
  return (
    <table className="w-full border-collapse border-spacing-2 border border-slate-500">
      <thead>
      <tr>
        <th className="border p-2 border-slate-600">UUID</th>
        <th className="border p-2 border-slate-600">Name</th>
        <th className="border p-2 border-slate-600">IP Address</th>
        <th className="border p-2 border-slate-600">Used CPU</th>
        <th className="border p-2 border-slate-600">Free Memory</th>
      </tr>
      </thead>
      <tbody>
        {shipData.map(ship => (
          <tr>
            <td className="border p-2 border-slate-700">{ship.id}</td>
            <td className="border p-2 border-slate-700">{ship.name}</td>
            <td className="border p-2 border-slate-700">{ship.ipAddress}</td>
            <td className="border p-2 border-slate-700">{Math.floor(ship.currentHardwareResourcesUsage.cpu * 100)} %</td>
            <td className="border p-2 border-slate-700">{ship.currentHardwareResourcesUsage.memory} mb</td>
          </tr>
          ))
        }
      </tbody>
    </table>
  )
}