package org.example.dao;

import org.example.dto.Order;
import org.example.dto.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public interface FlooringDao {

    /**
     * Returns a list of all orders in ordersMap
     *
     * @return
     */
    public List<Order> getAllOrders();

    /**
     * Returns a list of all orders matching param date in ordersMap
     *
     * @param date
     * @return
     */
    public List<Order> getOrdersByDate(LocalDate date);

    /**
     * Reads all order files into ordersMap
     *
     * @throws FlooringDaoException
     */
    public void importOrderData() throws FlooringDaoException;

    /**
     * Reads all product data from file into productsMap
     *
     * @throws FlooringDaoException
     */
    public void importProductData() throws FlooringDaoException;

    /**
     * Reads all tax data from file into taxMap
     *
     * @throws FlooringDaoException
     */
    public void importTaxData() throws FlooringDaoException;

    /**
     * Constructs a new order object using parameters and matching product/tax data from product/tax maps
     *
     * @param date
     * @param customerName
     * @param state
     * @param productType
     * @param area
     * @return
     */
    public Order createOrder(String date, String customerName, String state, String productType, BigDecimal area);

    /**
     * Adds passed in order object to ordersMap
     *
     * @param newOrder
     * @return
     */
    public int addOrder(Order newOrder);

    /**
     * Checks passed in state for existence in the taxMap
     *
     * @param state
     * @return
     */
    public boolean checkTaxCode(String state);

    /**
     * Returns a list of all products in the productsMap
     *
     * @return
     */
    public List<Product> getProducts();

    /**
     * Checks passed in productType for existence in the productsMap
     *
     * @param productType
     * @return
     */
    public boolean checkProductType(String productType);

    /**
     * Returns an order object that contains a matching date and name; null if it doesn't exist
     *
     * @param date
     * @param customerName
     * @return
     */
    public Order getOrderByNameDate(String date, String customerName);

    /**
     * Replaces an order object in the orderMap with passed in order object
     *
     * @param order
     * @throws FlooringDaoException
     */
    public void updateOrder(Order order) throws FlooringDaoException;

    /**
     * Calls order member function on passed in order object to re-evaluate
     * its different costs based on productType and state
     *
     * @param order
     */
    public void recalculateOrder(Order order);

    /**
     * Returns an order matching the passed in orderNumber and date
     *
     * @param orderNumber
     * @param date
     * @return
     */
    public Order getOrderByOrderNumberDate(String orderNumber, String date);

    /**
     * Removes an order from the orderMap matching the passed in order object
     *
     * @param order
     */
    public void removeOrder(Order order);

    /**
     * Exports order data into individual files named based on date
     *
     * @throws FlooringDaoException
     */
    public void exportOrderData() throws FlooringDaoException;

    /**
     * Exports all order data into a single text file
     *
     * @throws FlooringDaoException
     */
    public void exportBackupOrderData() throws FlooringDaoException;

}