package vn.edu.hcmuaf.fit.websubject.service;

import vn.edu.hcmuaf.fit.websubject.entity.Address;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    List<Address> getUserAddresses(Integer userId);

    Optional<Address> getAddressById(Integer id);

    Address createAddress(Address address);

    Address updateAddress(Integer id, Address address);

    void deleteAddress(Integer id);

    Optional<Address> getAddressDefaultByUserId(Integer id);

    void setDefaultAddress(Integer id);

    void resetDefaultOtherAddress(int user_id, int selected_address_id);

   boolean existsOrderWithAddress(Integer addressId);
}
