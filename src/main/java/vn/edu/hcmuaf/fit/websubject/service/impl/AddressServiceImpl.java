package vn.edu.hcmuaf.fit.websubject.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.websubject.entity.Address;
import vn.edu.hcmuaf.fit.websubject.entity.User;
import vn.edu.hcmuaf.fit.websubject.payload.others.CurrentTime;
import vn.edu.hcmuaf.fit.websubject.repository.AddressRepository;
import vn.edu.hcmuaf.fit.websubject.repository.UserRepository;
import vn.edu.hcmuaf.fit.websubject.service.AddressService;

import java.util.List;
import java.util.Optional;

import static vn.edu.hcmuaf.fit.websubject.payload.others.CurrentTime.getCurrentTimeInVietnam;

@Service
public class AddressServiceImpl implements AddressService {

    private static final Logger Log = Logger.getLogger(AddressServiceImpl.class);

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public List<Address> getUserAddresses(Integer userId) {
        return addressRepository.findByUserIdAndActiveTrueOrderByIsDefaultDesc(userId);
    }

    @Override
    public Optional<Address> getAddressById(Integer id) {
        return addressRepository.findById(id);
    }

    @Override
    public Address createAddress(Address address) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> userOptional = userRepository.findByUsername(customUserDetails.getUsername());
            if (userOptional.isEmpty()) {
                Log.warn("Người dùng " + customUserDetails.getUsername() + " không tồn tại");
                throw new RuntimeException("User not found");
            }
            User user = userOptional.get();
            address.setUser(user);
            address.setCreatedAt(getCurrentTimeInVietnam());
            address.setActive(true);
            Log.info("Người dùng " + user.getUsername() + " tạo địa chỉ mới");
            return addressRepository.save(address);
        } catch (Exception e) {
            Log.error("Lỗi khi tạo địa chỉ với lỗi " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Address updateAddress(Integer id, Address address) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> userOptional = userRepository.findByUsername(customUserDetails.getUsername());
            if (userOptional.isEmpty()) {
                Log.warn("Người dùng " + customUserDetails.getUsername() + " không tồn tại");
                throw new RuntimeException("User not found");
            }
            Optional<Address> existingAddressOptional = addressRepository.findById(id);
            if (existingAddressOptional.isEmpty()) {
                Log.warn("Địa chỉ với id #" + id + " không tồn tại");
                throw new RuntimeException("Address not found");
            }
            Address updatingAddress = existingAddressOptional.get();
            updatingAddress.setFullName(address.getFullName());
            updatingAddress.setPhoneNumber(address.getPhoneNumber());
            updatingAddress.setProvinceCity(address.getProvinceCity());
            updatingAddress.setCountyDistrict(address.getCountyDistrict());
            updatingAddress.setWardCommune(address.getWardCommune());
            updatingAddress.setHnumSname(address.getHnumSname());
            updatingAddress.setUpdatedAt(getCurrentTimeInVietnam());
            Log.info("Cập nhật địa chỉ với id #" + id);
            return addressRepository.save(updatingAddress);
        } catch (Exception e) {
            Log.error("Lỗi khi cập nhật địa chỉ với lỗi " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void deleteAddress(Integer id) {
        try {
            if (addressRepository.existsOrderWithAddress(id)) {
                Optional<Address> addressOptional = addressRepository.findById(id);
                if (addressOptional.isPresent()) {
                    Address address = addressOptional.get();
                    address.setActive(false);
                    address.setUpdatedAt(CurrentTime.getCurrentTimeInVietnam());
                    addressRepository.save(address);
                    Log.info("Xóa địa chỉ với id #" + id);
                } else {
                    throw new RuntimeException("Address not found with id " + id);
                }
            } else {
                addressRepository.deleteById(id);
                Log.info("Xóa địa chỉ với id #" + id);
            }
        } catch (Exception e) {
            Log.error("Lỗi khi xóa địa chỉ với lỗi " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Optional<Address> getAddressDefaultByUserId(Integer id) {
        return addressRepository.findByAddressWithDefault(id);
    }

    @Override
    public void setDefaultAddress(Integer id) {
        addressRepository.setDefaultAddress(id);
        Optional<Address> address = getAddressById(id);
        if (address.isPresent()) {
            int user_id = address.get().getUser().getId();
            addressRepository.resetDefaultOtherAddress(user_id, id);
        }
    }

    @Override
    public void resetDefaultOtherAddress(int user_id, int selected_address_id) {
        addressRepository.resetDefaultOtherAddress(user_id, selected_address_id);
    }

    @Override
    public boolean existsOrderWithAddress(Integer addressId) {
        return addressRepository.existsOrderWithAddress(addressId);
    }
}
