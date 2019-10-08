package hello.entities;

import org.springframework.stereotype.Component;

@Component
public class Address {
    private String country;
    private String city;
    
    public Address () {
        System.out.println(this.getClass() + "----Init");
    }
    
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "Address [country=" + country + ", city=" + city + "]";
    }
}
