package hello.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hello.entities.Address;

@Service
public class AddressService {
    @Autowired
    private Address address;
    
    public void show() {
        System.out.println("Address: " + address);
    }
}
