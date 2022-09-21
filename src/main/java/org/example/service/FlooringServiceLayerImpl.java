package org.example.service;

import org.example.dao.FlooringAuditDao;
import org.example.dao.FlooringDao;
import org.example.dao.FlooringDaoException;
import org.example.dto.Order;
import org.example.dto.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class FlooringServiceLayerImpl implements FlooringServiceLayer {
    private final FlooringDao dao;
    private final FlooringAuditDao auditDao;

    private static final String VALID_DATE_REGEX = "^(1[0-2]|0[1-9])-(3[01]|[12][0-9]|0[1-9])-[0-9]{4}$";
    private static final String VALID_NAME_REGEX = "^[A-Za-z\\s,.`]+$";
    private static final String VALID_STATE_REGEX = "^[A-Z][A-Z]+$";
    private static final String VALID_AREA_REGEX = "^([0-9]+\\.?[0-9]*|\\.[0-9]+)$";
    private static final String VALID_ORDER_NUMBER_REGEX = "^\\d+$";

    public FlooringServiceLayerImpl(FlooringDao dao, FlooringAuditDao auditDao) {
        this.dao = dao;
        this.auditDao = auditDao;
    }

    @Override
    public List<Order> getOrdersByDate(String date) throws InvalidInputException, FlooringDaoException {

        //Validate date format
        if (!date.matches(VALID_DATE_REGEX)) {
            auditDao.writeAuditEntry("ATTEMPTED TO DISPLAY ORDERS WITH AN INVALID DATE.");
            throw new InvalidInputException("Invalid date format");
        } else {
            auditDao.writeAuditEntry("ORDERS DISPLAYED FOR DATE " + date);
            return dao.getOrdersByDate(LocalDate.parse(date, DateTimeFormatter.ofPattern("MM-dd-yyyy")));
        }
    }

    @Override
    public void importAllData() throws FlooringDaoException {
        try {
            dao.importOrderData();
            dao.importProductData();
            dao.importTaxData();
            auditDao.writeAuditEntry("ALL ORDER, PRODUCT AND TAX DATA IMPORTED INTO COLLECTIONS.");
        } catch (FlooringDaoException e) {
            auditDao.writeAuditEntry("ORDER, PRODUCT, AND TAX DATA IMPORTS FAILED.");
            throw new FlooringDaoException(e.getMessage());
        }

    }

    @Override
    public Order createOrder(String date, String customerName, String state, String productType, String areaStr)
            throws DateAlreadyPassedException, InvalidInputException, TaxCodeViolationException, FlooringDaoException {

        BigDecimal area;
        //Convert string to BigDecimal with input validation
        if (!areaStr.matches(VALID_AREA_REGEX)) {
            throw new InvalidInputException("Invalid input for area.");
        } else {
            area = new BigDecimal(areaStr);
        }
        //Validate date format
        if (!date.matches(VALID_DATE_REGEX)) {
            throw new InvalidInputException("Invalid date format");
        }
        //Input validation for customer name (not blank, only valid characters)
        else if (!customerName.matches(VALID_NAME_REGEX)) {
            throw new InvalidInputException("The name entered contains invalid characters or is an empty field.");
        }
        //Verify date is in the future
        else if (LocalDate.parse(date, DateTimeFormatter.ofPattern("MM-dd-yyyy")).isBefore(LocalDate.now())) {
            throw new DateAlreadyPassedException("The date entered is before today's date.");
        } else if (!state.matches(VALID_STATE_REGEX)) {
            throw new InvalidInputException("The state was not entered properly.");
        }
        //Verify state exists in tax file
        else if (!dao.checkTaxCode(state)) {
            throw new TaxCodeViolationException("State entered is not present in the tax code file.");
        }
        //Verify the productType is a valid product
        else if (!dao.checkProductType(productType)) {
            throw new InvalidInputException("The product type entered does not exist.");
        }
        //everything is valid, create object and return it so a summary can be shown
        else {
            auditDao.writeAuditEntry("ORDER INPUT VALIDATED, CREATING ORDER OBJECT FOR VERIFICATION.");
            return dao.createOrder(date, customerName, state, productType, area);

        }
    }

    @Override
    public int submitOrder(Order order) throws FlooringDaoException {
        auditDao.writeAuditEntry("ORDER VERIFIED AND SUBMITTED");
        return dao.addOrder(order);
    }

    @Override
    public List<Product> getProducts() throws FlooringDaoException {
        auditDao.writeAuditEntry("LIST OF ALL PRODUCTS RETRIEVED");
        return dao.getProducts();
    }

    @Override
    public Order getOrderToEdit(String date, String customerName) throws InvalidInputException, NoSuchItemException, FlooringDaoException {
        //Validate date format
        if (!date.matches(VALID_DATE_REGEX)) {
            throw new InvalidInputException("Invalid date format.");
        }
        //Input validation for customer customerName (not blank, only valid characters)
        else if (!customerName.matches(VALID_NAME_REGEX)) {
            throw new InvalidInputException("The name entered contains invalid characters or is an empty field.");
        }

        Order order = dao.getOrderByNameDate(date, customerName);
        auditDao.writeAuditEntry("EDIT RETRIEVAL INPUT VALIDATED, ORDER OBJECT RETRIEVED");

        //Does the order exist
        if (order == null) {
            auditDao.writeAuditEntry("RETRIEVED A NULL ORDER OBJECT FOR EDIT.");
            throw new NoSuchItemException("There is not an order on " + date + " under the name " + customerName + ".");
        } else {
            auditDao.writeAuditEntry("ORDER OBJECT FOR EDIT EXISTS AND RETRIEVED.");
            return order;
        }
    }

    @Override
    public Order editOrder(Order order, String customerName, String state, String productType, String areaStr)
            throws InvalidInputException, TaxCodeViolationException, FlooringDaoException {
        BigDecimal area;

        //Convert string to BigDecimal
        if (areaStr.equals("")) {
            area = order.getArea();
        } else if (!areaStr.equals("") && !areaStr.matches(VALID_AREA_REGEX)) {
            throw new InvalidInputException("Invalid input for area.");
        } else {
            area = new BigDecimal(areaStr);
        }

        //Input validation for customerName (not blank, only valid characters)
        if (!customerName.equals("") && !customerName.matches(VALID_NAME_REGEX)) {
            throw new InvalidInputException("The name entered contains invalid characters or is an empty field.");
        } else if (!state.equals("") && !state.matches(VALID_STATE_REGEX)) {
            throw new InvalidInputException("The state was not entered properly.");
        }
        //Verify state exists in tax file
        else if (!state.equals("") && !dao.checkTaxCode(state)) {
            throw new TaxCodeViolationException("We cannot sell products in your state.");
        }
        //Verify the productType is a valid product
        else if (!productType.equals("") && !dao.checkProductType(productType)) {
            throw new InvalidInputException("The product type entered does not exist.");
        } else {
            auditDao.writeAuditEntry("NEW ORDER FIELDS FOR EDIT VALIDATED, SETTING FIELDS.");
            if (!areaStr.equals("")) {
                order.setArea(area);
            }
            if (!state.equals("")) {
                order.setState(state);
            }
            if (!productType.equals("")) {
                order.setProductType(productType);
            }
            if (!customerName.equals("")) {
                order.setCustomerName(customerName);
            }
            auditDao.writeAuditEntry("FIELDS SET, RECALULATING COST VALUES.");
            dao.recalculateOrder(order);
        }
        auditDao.writeAuditEntry("RETURNING ORDER OBJECT WITH UPDATED FIELDS AND COST VALUES.");
        return order;
    }

    @Override
    public void changeOrder(Order order) throws FlooringDaoException {
        auditDao.writeAuditEntry("EDITED ORDER BEING ADDED BACK TO COLLECTION IN PLACE OF OLD ORDER.");
        dao.updateOrder(order);
    }

    @Override
    public Order getOrderToRemove(String date, String orderNumber) throws InvalidInputException, NoSuchItemException, FlooringDaoException {
        //Validate date format
        if (!date.matches(VALID_DATE_REGEX)) {
            throw new InvalidInputException("Invalid date format.");
        }
        //Input validation for customer customerName (not blank, only valid characters)
        else if (!orderNumber.matches(VALID_ORDER_NUMBER_REGEX)) {
            throw new InvalidInputException("Invalid order number.");
        }
        auditDao.writeAuditEntry("ORDER REMOVAL INPUT VALIDATED, RETRIEVING ORDER OBJECT TO REMOVE.");
        Order order = dao.getOrderByOrderNumberDate(orderNumber, date);

        //Does the order exist
        if (order == null) {
            auditDao.writeAuditEntry("RETRIEVED A NULL ORDER OBJECT FOR REMOVAL.");
            throw new NoSuchItemException("There is not an order on " + date + " with the order number " + orderNumber + ".");
        } else {
            auditDao.writeAuditEntry("ORDER OBJECT FOR REMOVAL EXISTS AND RETRIEVED.");
            return order;
        }
    }

    @Override
    public void removeOrder(Order order) throws FlooringDaoException {
        dao.removeOrder(order);
        auditDao.writeAuditEntry("ORDER OBJECT REMOVED FROM COLLECTION.");
    }

    @Override
    public void exportAllData() throws FlooringDaoException {
        dao.exportOrderData();
        auditDao.writeAuditEntry("ORDER DATA EXPORTED TO ORDER TEXT FILES BY DATE.");
    }

    @Override
    public void exportBackupData() throws FlooringDaoException {
        dao.exportBackupOrderData();
        auditDao.writeAuditEntry("ORDER DATA EXPORTED TO BACKUP DATA FILE.");
    }

}