package org.example.service;

import org.example.dao.FlooringDaoException;
import org.example.dto.Order;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class FlooringServiceLayerImplTest {
    private FlooringServiceLayer service;

    public FlooringServiceLayerImplTest() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        service = ctx.getBean("service", FlooringServiceLayerImpl.class);
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testGetOrdersByDate() {
        List<Order> ordersByDate = null;
        try {
            ordersByDate = service.getOrdersByDate("01-01-2022");
        } catch (FlooringDaoException | InvalidInputException e) {
            fail("Exception should not occur");
        } finally {
            assertNotNull(ordersByDate);
        }
    }

    @Test
    public void testGetOrdersByDateInvalidInput() {
        List<Order> ordersByDate = null;
        try {
            ordersByDate = service.getOrdersByDate("01-1-2022");
        } catch (FlooringDaoException | InvalidInputException e) {
            assertEquals(e.getMessage(), "Invalid date format");
        } finally {
            assertNull(ordersByDate);
        }
        try {
            ordersByDate = service.getOrdersByDate("2021-02-13");
        } catch (FlooringDaoException | InvalidInputException e) {
            assertEquals(e.getMessage(), "Invalid date format");
        } finally {
            assertNull(ordersByDate);
        }
        try {
            ordersByDate = service.getOrdersByDate("01-05-2022");
        } catch (FlooringDaoException | InvalidInputException e) {
            fail("Exception should not occur");
        } finally {
            assertNotNull(ordersByDate);
            assertEquals(ordersByDate.size(), 0);
        }
    }

    @Test
    public void testAddOrder() {
        Order order = null;
        try {
            order = service.createOrder("05-05-2022", "Valid Test", "TX", "Tile", "50");
        } catch (FlooringDaoException | DateAlreadyPassedException | InvalidInputException |
                 TaxCodeViolationException e) {
            fail("Exception should not occur");
        } finally {
            assertNotNull(order);
            assertEquals(order.getCustomerName(), "Valid Test");
            order = null;
        }

        try {
            order = service.createOrder("05--2022", "Valid Test", "TX", "Tile", "50");
        } catch (FlooringDaoException | DateAlreadyPassedException | InvalidInputException |
                 TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "Invalid date format");
        } finally {
            assertNull(order);
        }

        try {
            order = service.createOrder("05-05-2022", "Valid Test2", "TX", "Tile", "50");
        } catch (FlooringDaoException | DateAlreadyPassedException | InvalidInputException |
                 TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "The name entered contains invalid characters or is an empty field.");
        } finally {
            assertNull(order);
        }

        try {
            order = service.createOrder("05-05-2022", "Valid Test", "TXs", "Tile", "50");
        } catch (FlooringDaoException | DateAlreadyPassedException | InvalidInputException |
                 TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "The state was not entered properly.");
        } finally {
            assertNull(order);
        }

        try {
            order = service.createOrder("05-05-2020", "Valid Test", "TX", "Tile", "50");
        } catch (FlooringDaoException | DateAlreadyPassedException | InvalidInputException |
                 TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "The date entered is before today's date.");
        } finally {
            assertNull(order);
        }

        try {
            order = service.createOrder("05-05-2022", "Valid Test", "VA", "Tile", "50");
        } catch (FlooringDaoException | DateAlreadyPassedException | InvalidInputException |
                 TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "State entered is not present in the tax code file.");
        } finally {
            assertNull(order);
        }

        try {
            order = service.createOrder("05-05-2022", "Valid Test", "TX", "Tile", "-50");
        } catch (FlooringDaoException | DateAlreadyPassedException | InvalidInputException |
                 TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "Invalid input for area.");
        } finally {
            assertNull(order);
        }

        try {
            order = service.createOrder("05-05-2022", "Valid Test", "TX", "Unicorn", "50");
        } catch (FlooringDaoException | DateAlreadyPassedException | InvalidInputException |
                 TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "The product type entered does not exist.");
        } finally {
            assertNull(order);
        }

        try {
            order = service.createOrder("05-05-2022", "Valid Test", "TX", "Unicorn", "asd");
        } catch (FlooringDaoException | DateAlreadyPassedException | InvalidInputException |
                 TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "Invalid input for area.");
        } finally {
            assertNull(order);
        }
    }

    @Test
    public void testGetOrderToEdit() {
        Order order = null;
        try {
            order = service.getOrderToEdit("01-01-2022", "Test Name");
        } catch (InvalidInputException | NoSuchItemException | FlooringDaoException e) {
            fail("Exception should not occur.");
        } finally {
            assertNotNull(order);
            assertEquals(order.getCustomerName(), "Test Name");
        }
        order = null;
        try {
            order = service.getOrderToEdit("011-2022", "Test Name");
        } catch (InvalidInputException | NoSuchItemException | FlooringDaoException e) {
            assertEquals(e.getMessage(), "Invalid date format.");
        } finally {
            assertNull(order);
        }

        try {
            order = service.getOrderToEdit("01-01-2022", "Test Name23");
        } catch (InvalidInputException | NoSuchItemException | FlooringDaoException e) {
            assertEquals(e.getMessage(), "The name entered contains invalid characters or is an empty field.");
        } finally {
            assertNull(order);
        }

        try {
            order = service.getOrderToEdit("01-01-2023", "Test Name");
        } catch (InvalidInputException | NoSuchItemException | FlooringDaoException e) {
            assertEquals(e.getMessage(), "There is not an order on " + "01-01-2023" + " under the name " + "Test Name" + ".");
        } finally {
            assertNull(order);
        }
    }

    @Test
    public void testEditOrder() {
        Order order = null;
        try {
            order = service.getOrderToEdit("01-01-2022", "Test Name");
        } catch (FlooringDaoException | InvalidInputException | NoSuchItemException e) {
            fail("Exception should not occur");
        }

        try {
            service.editOrder(order, "New Test Name", "WA", "Carpet", "200");
        } catch (FlooringDaoException | InvalidInputException | TaxCodeViolationException e) {
            fail("Exception should not occur");
        }


        try {
            service.editOrder(order, "New Test123", "WA", "Carpet", "200");
        } catch (FlooringDaoException | InvalidInputException | TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "The name entered contains invalid characters or is an empty field.");
        }


        try {
            service.editOrder(order, "New Test", "WAx", "Carpet", "200");
        } catch (FlooringDaoException | InvalidInputException | TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "The state was not entered properly.");
        }


        try {
            service.editOrder(order, "New Test", "VA", "Carpet", "200");
        } catch (FlooringDaoException | InvalidInputException | TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "We cannot sell products in your state.");
        }


        try {
            service.editOrder(order, "New Test", "TX", "Woody", "200");
        } catch (FlooringDaoException | InvalidInputException | TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "The product type entered does not exist.");
        }

        try {
            service.editOrder(order, "New Test", "TX", "Carpet", "2asd");
        } catch (FlooringDaoException | InvalidInputException | TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "Invalid input for area.");
        }


        try {
            service.editOrder(order, "", "", "", "");
        } catch (FlooringDaoException | InvalidInputException | TaxCodeViolationException e) {
            fail("Exception should not occur.");
        }

        try {
            service.editOrder(order, "", "", "", "-20");
        } catch (FlooringDaoException | InvalidInputException | TaxCodeViolationException e) {
            assertEquals(e.getMessage(), "Invalid input for area.");
        }
    }

    @Test
    public void testGetOrderToRemove() {
        try {
            service.getOrderToRemove("01-01-2022", "1");
        } catch (InvalidInputException | NoSuchItemException | FlooringDaoException e) {
            fail("Exception should not occur");
        }

        try {
            service.getOrderToRemove("011-2022", "1");
        } catch (InvalidInputException | NoSuchItemException | FlooringDaoException e) {
            assertEquals(e.getMessage(), "Invalid date format.");
        }

        try {
            service.getOrderToRemove("01-01-2022", "1a");
        } catch (InvalidInputException | NoSuchItemException | FlooringDaoException e) {
            assertEquals(e.getMessage(), "Invalid order number.");
        }

        try {
            service.getOrderToRemove("01-01-2022", "15");
        } catch (InvalidInputException | NoSuchItemException | FlooringDaoException e) {
            assertEquals(e.getMessage(), "There is not an order on " + "01-01-2022" + " with the order number " + "15" + ".");
        }

        try {
            service.getOrderToRemove("01-05-2022", "1");
        } catch (InvalidInputException | NoSuchItemException | FlooringDaoException e) {
            assertEquals(e.getMessage(), "There is not an order on " + "01-05-2022" + " with the order number " + "1" + ".");
        }
    }


}