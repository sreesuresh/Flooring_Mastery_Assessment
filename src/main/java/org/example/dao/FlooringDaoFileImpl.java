package org.example.dao;


import org.example.dto.Order;
import org.example.dto.Product;
import org.example.dto.Tax;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class FlooringDaoFileImpl implements FlooringDao {
    private final HashMap<Integer, Order> ordersMap = new HashMap<>();
    private final HashMap<String, Product> productMap = new HashMap<>();
    private final HashMap<String, Tax> taxMap = new HashMap<>();
    private final String PRODUCT_FILE, TAX_FILE, ORDER_FILE_PREFIX, ORDERS_DIR, BACKUP_ORDER_FILE;
    private static final String DELIMITER = "::";

    public FlooringDaoFileImpl(String ordersDir, String orderFilePrefix, String backupOrderFile, String productFile, String taxFile) {
        this.ORDERS_DIR = ordersDir;
        this.ORDER_FILE_PREFIX = orderFilePrefix;
        this.PRODUCT_FILE = productFile;
        this.TAX_FILE = taxFile;
        this.BACKUP_ORDER_FILE = backupOrderFile;
    }

    @Override
    public List<Order> getAllOrders() {
        return new ArrayList<>(ordersMap.values());
    }

    @Override
    public List<Order> getOrdersByDate(LocalDate date) {
        return getAllOrders().stream().filter((order) -> order.getOrderDate().equals(date))
                .collect(Collectors.toList());
    }

    @Override
    public void importOrderData() throws FlooringDaoException {

        File ordersDir = new File(ORDERS_DIR);
        String currentLine;
        Order order;

        try {
            for (File ordersFile : ordersDir.listFiles()) {
                Scanner scanner = new Scanner(new BufferedReader(new FileReader(ordersFile)));
                scanner.nextLine(); //Ignore header line
                while (scanner.hasNextLine()) {
                    currentLine = scanner.nextLine();
                    order = unmarshallOrder(currentLine);
                    String dateString = ordersFile.getName().replace(ORDER_FILE_PREFIX, "");
                    dateString = dateString.replace(".txt", "");
                    LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("MMddyyyy"));
                    date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
                    order.setOrderDate(date);
                    ordersMap.put(order.getOrderNumber(), order);
                }
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            throw new FlooringDaoException("Couldn't read order files");
        }

    }

    private Order unmarshallOrder(String orderAsText) {
        String orderTokens[] = orderAsText.split(DELIMITER);
        Order orderFromFile = new Order(orderTokens[0], orderTokens[1], orderTokens[2], new BigDecimal(orderTokens[3]),
                orderTokens[4], new BigDecimal(orderTokens[5]), new BigDecimal(orderTokens[6]), new BigDecimal(orderTokens[7]),
                new BigDecimal(orderTokens[8]), new BigDecimal(orderTokens[9]), new BigDecimal(orderTokens[10]),
                new BigDecimal(orderTokens[11]));
        return orderFromFile;
    }

    @Override
    public void exportOrderData() throws FlooringDaoException {
        PrintWriter out;
        String orderAsText;
        //Sort orders into HashMap with date as key, list of orders as value
        Map<LocalDate, List<Order>> ordersByDate = getOrdersByDateMap();
        //Per key, write into file with name ORDER_FILE_PREFIX + key as string MMddyyyy

        try {
            //Clean the directory in case of removed items
            cleanDirectory();

            for (List<Order> orderList : ordersByDate.values()) {
                //Get the date assosciated with the current list
                String dateStr = orderList.get(0).getOrderDate()
                        .format(DateTimeFormatter.ofPattern("MMddyyyy"));

                //Name the file using the assosciated date
                out = new PrintWriter(new FileWriter(new File(ORDERS_DIR, ORDER_FILE_PREFIX + dateStr + ".txt")));

                //Header line for file
                out.println("OrderNumber" + DELIMITER + "CustomerName" + DELIMITER + "State" + DELIMITER + "TaxRate" + DELIMITER + "ProductType"
                        + DELIMITER + "Area" + DELIMITER + "CostPerSquareFoot" + DELIMITER + "LaborCostPerSquareFoot"
                        + DELIMITER + "MaterialCost" + DELIMITER + "LaborCost" + DELIMITER + "Tax" + DELIMITER + "Total");
                out.flush();

                //Add each order for given date
                for (Order order : orderList) {
                    orderAsText = marshallOrder(order);
                    out.println(orderAsText);
                    out.flush();
                }
                out.close();
            }
        } catch (IOException e) {
            throw new FlooringDaoException("Couldn't write order files.");
        }

    }

    /*OrderNumber::CustomerName::State::TaxRate::ProductType
    ::Area::CostPerSquareFoot::LaborCostPerSquareFoot
    ::MaterialCost::LaborCost::Tax::Total*/
    private String marshallOrder(Order order) {
        String orderString = order.getOrderNumber() + DELIMITER + order.getCustomerName() + DELIMITER
                + order.getState() + DELIMITER + order.getTaxRate() + DELIMITER
                + order.getProductType() + DELIMITER + order.getArea() + DELIMITER
                + order.getCostPerSqFt() + DELIMITER + order.getLaborCostPerSqFt() + DELIMITER
                + order.getMaterialCost() + DELIMITER + order.getLaborCost() + DELIMITER
                + order.getTaxCost() + DELIMITER + order.getTotal();
        return orderString;
    }


    @Override
    public void importProductData() throws FlooringDaoException {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new BufferedReader(new FileReader(PRODUCT_FILE)));
        } catch (FileNotFoundException e) {
            throw new FlooringDaoException("Couldn't read product file");
        }

        String currentLine;
        Product product;

        scanner.nextLine(); //Ignore header line

        while (scanner.hasNextLine()) {
            currentLine = scanner.nextLine();
            product = unmarshallProduct(currentLine);
            productMap.put(product.getProductType(), product);
        }
        scanner.close();
    }

    private Product unmarshallProduct(String productAsText) {
        String productTokens[] = productAsText.split(DELIMITER);
        Product productFromFile = new Product(productTokens[0], new BigDecimal(productTokens[1]),
                new BigDecimal(productTokens[2]));
        return productFromFile;
    }

    @Override
    public void importTaxData() throws FlooringDaoException {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new BufferedReader(new FileReader(TAX_FILE)));
        } catch (FileNotFoundException e) {
            throw new FlooringDaoException("Couldn't read tax file");
        }

        String currentLine;
        Tax tax;

        scanner.nextLine(); //Ignore header line

        while (scanner.hasNextLine()) {
            currentLine = scanner.nextLine();
            tax = unmarshallTax(currentLine);
            taxMap.put(tax.getStateAbbrev(), tax);
        }

        scanner.close();
    }

    private Tax unmarshallTax(String taxAsText) {
        String[] taxTokens = taxAsText.split(DELIMITER);
        Tax taxFromFile = new Tax(taxTokens[0], taxTokens[1], new BigDecimal(taxTokens[2]));
        return taxFromFile;
    }

    @Override
    public int addOrder(Order newOrder) {

        //Get the highest order number and create a new one based off of it (by adding 1)
        if (!ordersMap.isEmpty()) {
            Order order = getAllOrders().stream().max(Comparator.comparing(var -> var.getOrderNumber())).get();
            int orderNumber = order.getOrderNumber() + 1;


            //Assign the new order number to a newly placed order and add the order to the map
            newOrder.setOrderNumber(orderNumber);
            ordersMap.put(newOrder.getOrderNumber(), newOrder);
            return orderNumber;
        } else {
            int orderNumber = 1;
            newOrder.setOrderNumber(orderNumber);
            ordersMap.put(newOrder.getOrderNumber(), newOrder);
            return orderNumber;
        }

    }

    @Override
    public Order createOrder(String date, String customerName, String state, String productType, BigDecimal area) {
        return new Order(customerName, state, productType, LocalDate.parse(date, DateTimeFormatter.ofPattern("MM-dd-yyyy")), area,
                taxMap.get(state).getTaxRate(), productMap.get(productType).getCostPerSqFt(), productMap.get(productType).getLaborCostPerSqFt());
    }

    @Override
    public boolean checkTaxCode(String state) {
        return taxMap.containsKey(state);
    }

    @Override
    public List<Product> getProducts() {
        return new ArrayList<>(productMap.values());
    }

    @Override
    public boolean checkProductType(String productType) {
        return productMap.containsKey(productType);
    }

    @Override
    public Order getOrderByNameDate(String date, String customerName) {
        List<Order> orderList = getAllOrders().stream().filter((ord) -> ord.getOrderDate().equals(LocalDate.parse(date, DateTimeFormatter.ofPattern("MM-dd-yyyy")))
                && ord.getCustomerName().equals(customerName)).collect(Collectors.toList());
        Order order = null;
        if (!orderList.isEmpty()) {
            order = orderList.get(0);
        }
        return order;
    }

    @Override
    public void updateOrder(Order order) throws FlooringDaoException {
        ordersMap.replace(order.getOrderNumber(), order);
        exportOrderData();
    }

    @Override
    public void recalculateOrder(Order order) {
        order.setCostPerSqFt(productMap.get(order.getProductType()).getCostPerSqFt());
        order.setLaborCostPerSqFt(productMap.get(order.getProductType()).getLaborCostPerSqFt());
        order.setTaxRate(taxMap.get(order.getState()).getTaxRate());
        order.recalculate();
    }

    @Override
    public Order getOrderByOrderNumberDate(String orderNumber, String date) {
        if (ordersMap.containsKey(Integer.parseInt(orderNumber)) && ordersMap.get(Integer.parseInt(orderNumber)).getOrderDate().equals(LocalDate.parse(date, DateTimeFormatter.ofPattern("MM-dd-yyyy")))) {
            return ordersMap.get(Integer.parseInt(orderNumber));
        } else {
            return null;
        }
    }

    @Override
    public void removeOrder(Order order) {
        ordersMap.remove(order.getOrderNumber(), order);
    }

    private Map<LocalDate, List<Order>> getOrdersByDateMap() {
        return getAllOrders().stream().collect(Collectors.groupingBy(order -> order.getOrderDate()));
    }

    @Override
    public void exportBackupOrderData() throws FlooringDaoException {
        PrintWriter out;

        try {
            out = new PrintWriter(new FileWriter(BACKUP_ORDER_FILE));
        } catch (IOException ex) {
            throw new FlooringDaoException("Cannot write to file");
        }
        //Header line for file
        out.println("OrderNumber" + DELIMITER + "CustomerName" + DELIMITER + "State" + DELIMITER + "TaxRate" + DELIMITER + "ProductType"
                + DELIMITER + "Area" + DELIMITER + "CostPerSquareFoot" + DELIMITER + "LaborCostPerSquareFoot"
                + DELIMITER + "MaterialCost" + DELIMITER + "LaborCost" + DELIMITER + "Tax" + DELIMITER + "Total" + DELIMITER + "Date");
        out.flush();
        String orderAsText;
        List<Order> orderList = this.getAllOrders();
        for (Order order : orderList) {
            orderAsText = marshallOrderWithDate(order);
            out.println(orderAsText);
            out.flush();
        }

        out.close();
    }

    private String marshallOrderWithDate(Order order) {
        String orderString = order.getOrderNumber() + DELIMITER + order.getCustomerName() + DELIMITER
                + order.getState() + DELIMITER + order.getTaxRate() + DELIMITER
                + order.getProductType() + DELIMITER + order.getArea() + DELIMITER
                + order.getCostPerSqFt() + DELIMITER + order.getLaborCostPerSqFt() + DELIMITER
                + order.getMaterialCost() + DELIMITER + order.getLaborCost() + DELIMITER
                + order.getTaxCost() + DELIMITER + order.getTotal() + DELIMITER + order.getOrderDate().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        return orderString;
    }

    private void cleanDirectory() throws IOException {
        File ordersDir = new File(ORDERS_DIR);

        for (File file : ordersDir.listFiles()) {
            file.delete();
        }
    }
}