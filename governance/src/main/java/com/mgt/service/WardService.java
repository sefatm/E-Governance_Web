package com.mgt.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mgt.dao.WardDAO;
import com.mgt.model.Ward;

@Service
public class WardService {

    @Autowired 
    WardDAO wardDAO;

    public Ward create(Ward ward) { 
    	return wardDAO.save(ward); }
    
    public Ward update(Ward ward) { 
    	return wardDAO.save(ward); }
    
    public List<Ward> getAll() { 
    	return wardDAO.getAll(); }

    public List<Ward> getAllWithBoundaries() {
    	return wardDAO.getAllWithBoundaries(); }
    
    public Ward getById(int id) { 
    	return wardDAO.getById(id); }

    public Ward getByNumber(int number) {
        return wardDAO.getByNumber(number);
    }
    
    public void updateStatus(int id, String s) { 
    	wardDAO.updateStatus(id, s); }
    
    public void delete(int id) { 
    	wardDAO.delete(id); }
}
