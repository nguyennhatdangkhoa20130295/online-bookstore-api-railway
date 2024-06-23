package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.websubject.entity.Address;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {

    List<Address> findByUserIdAndActiveTrueOrderByIsDefaultDesc(Integer userId);

    @Query("select a from Address a inner join User u on a.user.id = u.id where u.id = :userId and a.isDefault=true")
    Optional<Address> findByAddressWithDefault(Integer userId);

    @Transactional
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = true WHERE a.id =:id")
    void setDefaultAddress(Integer id);

    @Transactional
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = CASE WHEN a.id = :selected_address_id THEN true ELSE false END WHERE a.user.id = :user_id")
    void resetDefaultOtherAddress(int user_id, int selected_address_id);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN TRUE ELSE FALSE END FROM Order o WHERE o.shippingAddress.id = :addressId")
    boolean existsOrderWithAddress(@Param("addressId") Integer addressId);
}
