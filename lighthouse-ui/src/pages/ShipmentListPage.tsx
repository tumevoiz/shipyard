import React, {useEffect, useState} from 'react';
import {DefaultService, Ship, Shipment, ShipmentHealth} from "../generated";
import {isAPIError} from "../utils";

export const ShipmentListPage: React.FC = () => {
  const [shipmentData, setShipmentData] = useState<Array<Shipment>>([])
  const [shipData, setShipData] = useState<Array<Ship>>([])

  const [formShipmentName, setFormShipmentName] = useState("");
  const [formShipmentImage, setFormShipmentImage] = useState("");
  const [formShipmentCpu, setFormShipmentCpu] = useState(0);
  const [formShipmentMemory, setFormShipmentMemory] = useState(0);

  const circleColorBasedOnHealth = (status: ShipmentHealth | undefined): string => {
    switch(status) {
      case ShipmentHealth.DETACHED:
        return "bg-red-600";
      case ShipmentHealth.UNAVAILABLE:
        return "bg-yellow-600";
      case ShipmentHealth.RUNNING:
        return "bg-green-600";
      default:
        return "animate-ping bg-sky-800"
    }
  }

  const buildShipmentAccessUrl = (shipId: string): string => {
    const ship = shipData.filter(s => s.id === shipId)[0]
    if (ship == null) {
      return `Unable to find URL: ${shipData.join(",")}`
    }

    return `http://${ship.ipAddress}:8010/`
  }

  const addShipmentPromise = async() => {
    const addShipmentRequest = await DefaultService.postApiShipments({
      name: formShipmentName,
      image: formShipmentImage,
      hardwareRequirements: {
        cpu: formShipmentCpu,
        memory: formShipmentMemory
      },
    })
  }

  const deleteShipmentPromise = async(shipmentId: string) => {
    const deleteShipmentRequest = await DefaultService.putApiShipmentsShipmentidHealth(shipmentId, ShipmentHealth.DETACHED)
  }

  const shipPromise = async() => {
    const ships = await DefaultService.getApiShips()
    if(isAPIError(ships)) {
      console.error("API error!")
      return;
    }

    setShipData(ships)
  }

  const shipmentPromise = async() => {
    const shipments = await DefaultService.getApiShipments()
    if(isAPIError(shipments)) {
      console.error("API error!")
      return;
    }

    setShipmentData(shipments)
  }


  useEffect(() => {
    shipPromise().catch(console.error)
    shipmentPromise().catch(console.error)
    const interval = setInterval(() => {
      shipPromise().catch(console.error)
      shipmentPromise().catch(console.error)
    }, 5000)
    return () => clearInterval(interval)
  }, [])

  return (
    <>
      <main className="flex flex-col gap-4">
        <table className="w-full border-collapse border-spacing-2 border border-slate-500">
          <thead>
          <tr>
            <th className="border p-2 border-slate-600">Name</th>
            <th className="border p-2 border-slate-600">Image</th>
            <th className="border p-2 border-slate-600">Requested CPU</th>
            <th className="border p-2 border-slate-600">Requested Memory</th>
            <th className="border p-2 border-slate-600">UUID</th>
            <th className="border p-2 border-slate-600">Assigned ship ID</th>
            <th className="border p-2 border-slate-600">Application URL</th>
            <th className="border p-2 border-slate-600">Actions</th>
          </tr>
          </thead>
          <tbody>
          {shipmentData.map(shipment => (
            <tr>
              <td className="border p-2 border-slate-700 flex flex-row gap-1">
                <span className={`relative mt-2.5 h-1.5 w-1.5 rounded-full ${circleColorBasedOnHealth(shipment.health)}`}>&nbsp;</span>
                {shipment.name}
              </td>
              <td className="border p-2 border-slate-700">{shipment.image}</td>
              <td className="border p-2 border-slate-700">{shipment.hardwareRequirements?.cpu} %</td>
              <td className="border p-2 border-slate-700">{shipment.hardwareRequirements?.memory} mb</td>
              <td className="border p-2 border-slate-700">{shipment.id}</td>
              <td className="border p-2 border-slate-700">{shipment.shipId}</td>
              <td className="border p-2 border-slate-700">
                {<a href={buildShipmentAccessUrl(shipment.shipId!)}>{buildShipmentAccessUrl(shipment.shipId!)}</a>}
              </td>
              <td className="border p-2 border-slate-700"><button onClick={async () => await deleteShipmentPromise(shipment.id!)}>Delete</button></td>

            </tr>
          ))
          }
          </tbody>
        </table>
        <hr/>
        <div className="flex flex-col">
          <span className="text-3xl font-bold">Schedule new shipment</span> <br/>
          <form className="flex flex-col gap-3">
            <span className="text-lg font-bold">Metadata</span>
            Name: <input onChange={e => setFormShipmentName(e.target.value)} className="border" type="text" name="name"/>
            Image: <input  onChange={e => setFormShipmentImage(e.target.value)} className="border" type="text" name="image"/>
            <span className="text-lg font-bold">Hardware requirements</span>
            CPU (in ms): <input  onChange={e => setFormShipmentCpu(Number.parseInt(e.target.value))} className="border" type="text" name="cpu"/>
            Memory (in MB): <input  onChange={e => setFormShipmentMemory(Number.parseInt(e.target.value))} className="border" type="text" name="memory"/>
            <button onClick={async () => await addShipmentPromise()} className="px-6 py-2 bg-amber-400 border">Add</button>
          </form>
        </div>
      </main>
    </>
  )
}