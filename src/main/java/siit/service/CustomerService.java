package siit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import siit.db.ProductDao;
import siit.exceptions.ValidationException;
import siit.model.Customer;
import siit.db.CustomerDao;
import siit.db.OrderDao;
import siit.model.Order;
import siit.model.OrderProduct;
import siit.model.Product;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ProductDao productDao;

    public List<Customer> getCustomers(){
        return customerDao.getCustomers();
    }

    public Customer getCustomerByIdWithOrders(int id) {
        Customer customer = customerDao.getCustomerById(id);
        customer.setOrders(orderDao.getOrdersByCustomerId(id));
        Map<Integer, Product> productMap = new HashMap<>();
        for (Order order : customer.getOrders()) {
            populateOrderProducts(order, productMap);
            populateOrderValue(order);
        }
        return customer;
    }

    @Transactional
    public void deleteOrder(int id) {
        orderDao.deleteOrderProductsByOrderId(id);
        orderDao.deleteOrderById(id);
    }

    public void updateCustomer(Customer customer){
        if (!customer.getPhone().matches("\\+\\d+")){
            throw new ValidationException("phoneNumber.malformed");
        }
        customerDao.update(customer);
    }

    public Order getOrderById(int id){
        Order order = orderDao.findOrderById(id);
        populateOrderProducts(order, new HashMap<>());
        return order;
    }

    private void populateOrderValue(Order order) {
        BigDecimal orderTotalValue = BigDecimal.ZERO;
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            orderTotalValue = orderTotalValue.add(orderProduct.getValue());
        }
        order.setValue(orderTotalValue);
    }

    public void addOrderForCustomer(int customerId, Order order){
        orderDao.addOrderForCustomer(customerId, order);
    }

    private void populateOrderProducts(Order order, Map<Integer, Product> productMap) {
        order.setOrderProducts(
                orderDao.getOrderProductsForOrder(order.getId()));
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            Product product = productMap.computeIfAbsent(
                    orderProduct.getProduct().getId(), productDao::findById);
            orderProduct.setProduct(product);
        }
    }

    public OrderProduct addOrderProduct(OrderProduct orderProduct) {
        OrderProduct existingOrderProduct = getOrderProductByProductId(orderProduct.getOrderId(), orderProduct.getProduct().getId());
        if (existingOrderProduct == null) {
            orderDao.addProductToOrder(orderProduct);
            return getOrderProductByProductId(orderProduct.getOrderId(), orderProduct.getProduct().getId());
        } else {
            existingOrderProduct.setQuantity(existingOrderProduct.getQuantity() + orderProduct.getQuantity());
            orderDao.updateOrderProduct(existingOrderProduct);
            return existingOrderProduct;
        }
    }

    private OrderProduct getOrderProductByProductId(long orderId, int productId) {
        List<OrderProduct> orderProducts = orderDao.getOrderProductsForOrder(orderId);
        for (OrderProduct dbOrderProduct : orderProducts) {
            if (productId == dbOrderProduct.getProduct().getId()) {
                dbOrderProduct.setProduct(productDao.findById(dbOrderProduct.getProduct().getId()));
                return dbOrderProduct;
            }
        }
        return null;
    }
}
