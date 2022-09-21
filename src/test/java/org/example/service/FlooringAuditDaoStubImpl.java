package org.example.service;

import org.example.dao.FlooringAuditDao;
import org.example.dao.FlooringDaoException;

public class FlooringAuditDaoStubImpl implements FlooringAuditDao {

    @Override
    public void writeAuditEntry(String entry) throws FlooringDaoException {
    }

}