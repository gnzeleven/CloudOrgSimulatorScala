package Simulations

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import HelperUtils.{ObtainConfigReference, Utils}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology
import org.cloudbus.cloudsim.vms.Vm

import scala.collection.JavaConverters.*
import scala.collection.mutable.ListBuffer

class SimulationsTestSuite extends AnyFlatSpec with Matchers {

  // Create config objects for simulation1, simulation2, and simulation3
  val config1, config2, config3 = (1 to 3).map(
    i => ObtainConfigReference("simulation" + i) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
  )

  val simulation: CloudSim = new CloudSim()
  val broker0: DatacenterBrokerSimple = new DatacenterBrokerSimple(simulation)

  // SIMULATION 1
  val cnf1 = config1(0).getConfig("simulation1")

  // Test whether VMs are created recursively as per the parameter num_vm
  it should "Number of VMs created should match with application.conf" in {
    val num_vm = cnf1.getInt("vm.num")
    val vmList = Simulation1.createVms(num_vm)
    assert(vmList.length == num_vm)
  }

  // Test whether recursive VM creation stops appropriately
  it should "Given num_vm=1 it should create 1 VM and stop" in {
    val num_vm = 1
    val vmList = new ListBuffer[Vm]
    Simulation1.vmRecursive(num_vm, 0, vmList)
    assert(vmList.toList.length == 1)
  }

  // Test if network is added to a datacenter
  it should "Check whether Network Topology is added" in {
    val hostList = new ListBuffer[Host]
    Simulation1.hostRecursive(3, 1, hostList)
    val dc = new DatacenterSimple(simulation, hostList.asJava, new VmAllocationPolicySimple())
    Simulation1.setUpNetworkTopology(dc, broker0, simulation)
    assert(simulation.getNetworkTopology().isInstanceOf[BriteNetworkTopology])
  }

  // SIMULATION 2
  val cnf2 = config2(0).getConfig("simulation2")

  // Test what service is encoded for simulation2 in application.conf
  it should "Service type should be IaaS or PaaS or SaaS" in {
    val service = cnf2.getString("service")
    assert(service == "IaaS" || service == "PaaS" || service == "SaaS")
  }

  // Number of datacenters in simulation2
  it should "Number of dcs created should match with application.conf" in {
    val num_dc = cnf2.getInt("dc.num")
    val dcList = Utils.createDatacenters(num_dc, simulation, "simulation2")
    assert(dcList.length == num_dc)
  }

  // Number of hosts in dc1 in simulation2
  it should "Number of hosts created in dc1 should match with application.conf" in {
    val num_hosts = cnf2.getInt("1.host.num")
    val dc = Utils.createDatacenter(1, simulation, "simulation2")
    assert(dc.getHostList.size() == num_hosts)
  }

  // Number of PEs in a host in dc2 in simulation2
  it should "Number of PEs in a host in dc2 should match with application.conf" in {
    val PE = cnf2.getInt("1.host.PE")
    val host = Utils.createHost(1, 1, "simulation2")
    assert(host.getPeList.size() == PE)
  }

  // SIMULATION 3
  val cnf3 = config3(0).getConfig("simulation3")

  // Number of VMs in simulation3
  it should "Number of VMs in simulation3 should match with application.conf" in {
    val num_vm = cnf3.getInt("vm.num")
    val vmList = Utils.createVms(num_vm, "simulation3")
    assert(vmList.length == num_vm)
  }

  // Number of cloudlets in simulation3
  it should "Number of cloudlets in simulation3 should match with application.conf" in {
    val num_cloudlet = cnf3.getList("cloudlet.num")
    val cloudletList = Utils.createCloudlets("simulation3")
    assert(cloudletList.length == num_cloudlet.unwrapped().get(0).asInstanceOf[Int])
  }

  // Length of cloudlets in simulation3
  it should "Length of cloudlets in simulation3 should match with application.conf" in {
    val length = cnf3.getList("cloudlet.length")
    val cloudlet = Utils.createCloudlet(1, "simulation3")
    assert(cloudlet.getLength == length.unwrapped().get(0).asInstanceOf[Int])
  }
}
