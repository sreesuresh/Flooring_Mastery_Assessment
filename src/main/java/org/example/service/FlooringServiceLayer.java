package org.example.service;

import org.example.dao.FlooringDaoException;
import org.example.dto.Order;
import org.example.dto.Product;

import java.util.List;

public interface FlooringServiceLayer {

    /**
     * Validates the string "date" against a date regex,
     * returns a list of orders with that date if valid,
     * throws InvalidInputException if the date is not valid
     *
     * @param date
     * @return
     * @throws InvalidInputException
     * @throws FlooringDaoException
     */
    public List<Order> getOrdersByDate(String date) throws InvalidInputException, FlooringDaoException;

    /**
     * Validates all parameter inputs against regex and other requirements
     * and returns a newly created order object using the parameters
     *
     * @param date
     * @param customerName
     * @param state
     * @param productType
     * @param area
     * @return
     * @throws DateAlreadyPassedException
     * @throws InvalidInputException
     * @throws TaxCodeViolationException
     * @throws FlooringDaoException
     */
    public Order createOrder(String date, String customerName, String state, String productType, String area) throws DateAlreadyPassedException,
            InvalidInputException, TaxCodeViolationException, FlooringDaoException;

    /**
     * Pass through to add an order to the ordersMap in dao
     *
     * @param order
     * @return
     * @throws FlooringDaoException
     */
    public int submitOrder(Order order) throws FlooringDaoException;

    /**
     * Gets a list of all products from the dao productsMap.
     *
     * @return
     * @throws FlooringDaoException
     */
    public List<Product> getProducts() throws FlooringDaoException;

    /**
     * Calls dao data import methods for orders, products and taxes.
     *
     * @throws FlooringDaoException
     */
    public void importAllData() throws FlooringDaoException;

    /**
     * Gets an order from the dao orderMap with a matching date and customer customerName
     * after validating that the date and customerName are valid inputs.
     *
     * @param date
     * @param customerName
     * @return
     * @throws InvalidInputException
     * @throws NoSuchItemException
     * @throws FlooringDaoException
     */
    public Order getOrderToEdit(String date, String customerName) throws InvalidInputException, NoSuchItemException, FlooringDaoException;

    /**
     * Validates all parameter inputs according to regex and business logic
     * and updates/calculates new values for order fields and returns the object
     *
     * @param order
     * @param customerName
     * @param state
     * @param productType
     * @param area
     * @return
     * @throws InvalidInputException
     * @throws TaxCodeViolationException
     * @throws FlooringDaoException
     */
    public Order editOrder(Order order, String customerName, String state, String productType, String area) throws InvalidInputException, TaxCodeViolationException, FlooringDaoException;

    /**
     * Takes an order object as parameter and passes through to dao
     * to replace an existing order object in the ordersMap
     *
     * @param order
     * @throws FlooringDaoException
     */
    public void changeOrder(Order order) throws FlooringDaoException;

    /**
     * Gets an order based on the date and orderNumber and returns it to
     * the user.
     *
     * @param date
     * @param orderNumber
     * @return
     * @throws InvalidInputException
     * @throws NoSuchItemException
     * @throws FlooringDaoException
     */
    public Order getOrderToRemove(String date, String orderNumber) throws InvalidInputException, NoSuchItemException, FlooringDaoException;

    /**
     * Pass through to remove the matching order object from orderMap in dao
     *
     * @param order
     * @throws FlooringDaoException
     */
    public void removeOrder(Order order) throws FlooringDaoException;

    /**
     * Pass through for dao export of all data
     *
     * @throws FlooringDaoException
     */
    public void exportAllData() throws FlooringDaoException;

    /**
     * Pass through for dao export of backup data
     *
     * @throws FlooringDaoException
     */
    public void exportBackupData() throws FlooringDaoException;

}