package org.example.controller;

import org.example.dao.FlooringDaoException;
import org.example.dto.Order;
import org.example.service.*;
import org.example.view.FlooringView;

import static org.springframework.util.StringUtils.capitalize;

public class FlooringController {
    private final FlooringServiceLayer service;
    private final FlooringView view;

    public FlooringController(FlooringServiceLayer service, FlooringView view) {
        this.service = service;
        this.view = view;
    }

    public void run() {
        try {
            service.importAllData();

            boolean exit = false;
            while (!exit) {
                switch (view.displayGetMenuChoice()) {
                    case 1:
                        displayOrdersByDate();
                        break;
                    case 2:
                        createNewOrder();
                        break;
                    case 3:
                        editOrder();
                        break;
                    case 4:
                        removeOrder();
                        break;
                    case 5:
                        exportBackupData();
                        break;
                    case 6:
                        exit = true;
                        break;
                    //Quit
                }
            }

            service.exportAllData();
        } catch (FlooringDaoException e) {
            view.displayErrorMessage(e.getMessage());
        }
    }

    private void displayOrdersByDate() {
        boolean done = false;
        while (!done) {
            try {
                view.displayOrders(service.getOrdersByDate(view.getDate()));
                done = true;
            } catch (InvalidInputException e) {
                view.displayErrorMessage(e.getMessage());
            } catch (FlooringDaoException e) {
                view.displayErrorMessage(e.getMessage());
                done = true;
            }
        }
    }

    private void createNewOrder() {
        boolean done = false;
        while (!done) {
            try {
                Order order = service.createOrder(view.getDate(), view.getName(), view.getState(), capitalize(view.getProductType(service.getProducts())), view.getArea());
                view.displayOrderSummary(order);
                boolean valid;
                do {
                    String yesNo = view.getOrderConfirmation();
                    switch (yesNo) {
                        case "y":
                            view.displayOrderNumber(service.submitOrder(order));
                            view.displayOrderSubmissionSuccess();
                            valid = true;
                            break;
                        case "n":
                            view.displayOrderNotSubmitted();
                            valid = true;
                            break;
                        default:
                            view.displayInvalidChoice();
                            valid = false;
                            break;
                    }
                } while (!valid);

                done = true;
            } catch (DateAlreadyPassedException | FlooringDaoException | TaxCodeViolationException e) {
                view.displayErrorMessage(e.getMessage());
                done = true;
            } catch (InvalidInputException e) {
                view.displayErrorMessage(e.getMessage());
            }
        }
    }

    private void editOrder() {
        Order order = null;
        boolean done = false;

        while (!done) {
            try {
                order = service.getOrderToEdit(view.getDate(), view.getName());
                done = true;
            } catch (InvalidInputException e) {
                view.displayErrorMessage(e.getMessage());
            } catch (NoSuchItemException | FlooringDaoException e) {
                view.displayErrorMessage(e.getMessage());
                return;
            }
        }

        done = false;
        while (!done) {
            try {
                view.displayOrderEditMessage();
                view.displayOrderSummary(order);
                view.displayOrderEditInstructions();
                order = service.editOrder(order, view.getName(), view.getState(), capitalize(view.getProductType(service.getProducts())), view.getArea());
                view.displayOrderSummary(order);
                boolean valid;
                do {
                    String yesNo = view.getOrderConfirmation();
                    switch (yesNo) {
                        case "y":
                            service.changeOrder(order);
                            view.displayOrderSubmissionSuccess();
                            valid = true;
                            break;
                        case "n":
                            view.displayOrderNotSubmitted();
                            valid = true;
                            break;
                        default:
                            view.displayInvalidChoice();
                            valid = false;
                            break;
                    }
                } while (!valid);
                done = true;
            } catch (InvalidInputException e) {
                view.displayErrorMessage(e.getMessage());

            } catch (TaxCodeViolationException | FlooringDaoException e) {
                view.displayErrorMessage(e.getMessage());
                done = true;
            }
        }
    }

    private void removeOrder() {
        Order order = null;
        boolean done = false;

        while (!done) {
            try {
                order = service.getOrderToRemove(view.getDate(), view.getOrderNumber());
                done = true;
            } catch (InvalidInputException e) {
                view.displayErrorMessage(e.getMessage());
            } catch (NoSuchItemException | FlooringDaoException e) {
                view.displayErrorMessage(e.getMessage());
                return;
            }
        }

        boolean valid;
        do {
            view.displayOrderSummary(order);
            String yesNo = view.getRemoveOrderConfirmation();
            switch (yesNo) {
                case "y": {
                    try {
                        service.removeOrder(order);
                        view.displayRemoveOrderSuccess();
                        valid = true;
                        break;
                    } catch (FlooringDaoException e) {
                        view.displayErrorMessage(e.getMessage());
                    }
                }
                case "n":
                    view.displayOrderNotRemoved();
                    valid = true;
                    break;
                default:
                    view.displayInvalidChoice();
                    valid = false;
                    break;
            }
        } while (!valid);
    }

    private void exportBackupData() {
        try {
            service.exportBackupData();
            view.displayBackupSuccess();
        } catch (FlooringDaoException e) {
            view.displayErrorMessage(e.getMessage());
        }
    }
}