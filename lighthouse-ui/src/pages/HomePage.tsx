import React, {useEffect, useState} from 'react';
import {ClusterStats, DefaultService} from "../generated";
import {isAPIError} from "../utils";

export const HomePage: React.FC = () => {
  const [clusterStats, setClusterStats] = useState<ClusterStats | null>(null)
  const statsPromise = async() => {
    const shipments = await DefaultService.getApiLighthouseClusterStats()
    if(isAPIError(shipments)) {
      console.error("API error!")
      return;
    }

    setClusterStats(shipments)
  }


  useEffect(() => {
    statsPromise().catch(console.error)
    const interval = setInterval(() => {
      statsPromise().catch(console.error)
    }, 5000)
    return () => clearInterval(interval)
  }, [])

  return (
    <div className="flex flex-col gap-8 mt-4">
      <div className="flex flex-col gap-2">
        <span className="text-3xl font-semibold">Welcome!</span>
        Witaj w panelu kontrolnym oprogramowania Shipyard!
      </div>

      <div className="flex flex-col gap-2">
      <span className="text-3xl font-semibold">Cluster stats</span>
      <ul className="flex flex-row gap-16">
        <li className="flex flex-col"><span className="text-3xl font-bold">{clusterStats == null ? "???" : clusterStats.requestedMemory} mb</span><span className="font-light">free memory</span></li>
        <li className="flex flex-col"><span className="text-3xl font-bold">{clusterStats == null ? "???" : clusterStats.minusPredictedMemory} mb</span><span className="font-light">reserved memory</span></li>
        <li className="flex flex-col"><span className={`text-3xl font-bold ${clusterStats != null && clusterStats.requestedMemory - clusterStats.minusPredictedMemory >= 0 ? "text-emerald-600" : "text-amber-700"}`}>{clusterStats == null ? "???" : clusterStats.requestedMemory - clusterStats.minusPredictedMemory} mb</span><span className="font-light">available memory</span></li>
        <li className="flex flex-col"><span className="text-3xl font-bold">{clusterStats == null ? "???" : Math.floor(clusterStats.requestedCpuPercentage * 100)} %</span><span className="font-light">CPU used</span></li>
        <li className="flex flex-col"><span className="text-3xl font-bold">{clusterStats == null ? "???" : clusterStats.shipCount}</span><span className="font-light">ships</span></li>
        <li className="flex flex-col"><span className="text-3xl font-bold">{clusterStats == null ? "???" : clusterStats.shipmentsCount}</span><span className="font-light">shipments</span></li>

      </ul>
      </div>
    </div>
  )
}