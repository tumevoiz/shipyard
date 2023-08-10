package shipyard

import com.sun.management.OperatingSystemMXBean

import java.lang.management.ManagementFactory

object StatsCheck extends App {
  val osBeans: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean().asInstanceOf[OperatingSystemMXBean]
  val memory = osBeans.getFreePhysicalMemorySize / 1024 / 1024
  val cpu = osBeans.getSystemCpuLoad

  val memoryRuntime = Runtime.getRuntime.freeMemory()

  println(memory)
  println(cpu)

}
