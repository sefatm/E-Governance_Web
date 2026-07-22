package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.WaterBill;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;


@Repository(value = "waterBillDAO")
@Transactional
public class WaterBillDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public void save(WaterBill bill) {
		entityManager.persist(bill);
	}
	
	public List<WaterBill> getall() {
		String sql = "from WaterBill";
		List<WaterBill> bills = entityManager.createQuery(sql).getResultList();
		return bills;
	}
	
	public WaterBill getById(int id) {
        return entityManager.find(WaterBill.class, id);
    }

	public List<WaterBill> findByMeterAndMobile(String meterNo, String mobile) {
        return entityManager.createQuery(
                "from WaterBill w where lower(w.meterNo) = lower(:meterNo) and w.mobile = :mobile order by w.createdAt desc",
                WaterBill.class)
            .setParameter("meterNo", meterNo)
            .setParameter("mobile", mobile)
            .getResultList();
    }

	public void updateStatus(int id, String status) {
		WaterBill bill = entityManager.find(WaterBill.class, id);
        if (bill != null) {
        	bill.setStatus(status);
            entityManager.merge(bill);
        }
	}
	
	public void update(WaterBill bill) {
		entityManager.merge(bill);
	}
	
	public void delete(int id) {
		entityManager.createQuery("delete from WaterBill where id = :id")
		.setParameter("id", id)
		.executeUpdate();
	}
}
