Vagrant.configure("2") do |config|
    config.vm.define "node1" do |master|
        master.vm.box = "ubuntu/jammy64"
        master.vm.network "private_network", ip: "172.16.16.10",
                            virtualbox__intnet: "shipyard"
        master.vm.hostname = "node1"
        master.vm.provision :shell, path: "vagrant-scripts/01-prepare-environment.sh"
    end

    config.vm.define "node2" do |worker1|
        worker1.vm.box = "ubuntu/jammy64"
        worker1.vm.network "private_network", ip: "172.16.16.20",
                            virtualbox__intnet: "shipyard"
        worker1.vm.hostname = "node2"
        worker1.vm.provision :shell, path: "vagrant-scripts/01-prepare-environment.sh"
    end

    config.vm.provider "virtualbox" do |v|
        v.memory = 2048
        v.cpus = 2
        v.customize ["modifyvm", :id, "--nicpromisc2", "allow-all"]
    end
end