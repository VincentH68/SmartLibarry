package com.example.bookstore.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.example.bookstore.common.Constants;
import com.example.bookstore.entity.Address;
import com.example.bookstore.entity.Discount;
import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.Product;
import com.example.bookstore.model.CartModel;
import com.example.bookstore.service.*;
import com.example.bookstore.service.impl.MailerServiceImpl;
import com.example.bookstore.service.impl.ShoppingCartServiceImpl;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class CheckOutController {
	@Autowired
	AddressService addressService;


	@Autowired
	ShoppingCartServiceImpl cartService;
	
	@Autowired
	DiscountService discountService;
	
	@Autowired
	SessionService sessionService;
	
	@Autowired
	ParamService paramService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	ProductService productService;
	
	@Autowired
	MailerServiceImpl mailerService;
	private String addressId = "";
	private String comment = "";
	private String method = "";

	@GetMapping("/shop/cart/checkout")
	public String index(Model model) {
		List<CartModel> listCartModel = new ArrayList<>(cartService.getItems());
		if(listCartModel.isEmpty()) {
			return "redirect:/shop/cart";
		}
		model.addAttribute("cart", cartService);
		return Constants.USER_DISPLAY_CHECKOUT;
	}

	@PostMapping("/shop/cart/checkout")
	public String order(Model model) throws UnsupportedEncodingException {
		String addressId = paramService.getString("address_id", "");
		String method = paramService.getString("shipping_method", "");
		String comment = paramService.getString("comment", null);
		String paymentMethod = paramService.getString("paymentMethod", null);

		this.addressId = addressId;
		this.method = method;
		this.comment = comment;

		if(paymentMethod.equals("COD")) {
			Address address = addressService.getAddressById(Integer.parseInt(addressId));
			Discount discount = cartService.getDiscount();

			if(discount.getId() == 0) {
				discount = null;
			}

			int code;
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = formatter.format(date);

			while (true) {
				code = (int) Math.floor(((Math.random() * 899999) + 100000));
				List<Order> list = orderService.getOrderByName(String.valueOf(code));
				if (list.isEmpty()) {
					break;
				}
			}

			List<CartModel> listCartModel = new ArrayList<>(cartService.getItems());
			for(CartModel cart: listCartModel) {
				Order order = new Order();
				Product product = cart.getProduct();
				order.setCode(String.valueOf(code));
				order.setAddress(address);
				order.setDiscount(discount);
				order.setProduct(product);
				order.setQuality(cart.getQuality());
				order.setDate(strDate);
				order.setMethod(method);
				order.setStatus("0");
				order.setComment(comment);
				orderService.save(order);

				product.setQuality(product.getQuality() - cart.getQuality());
				productService.updateQuality(product);
			}

			discountService.updateQuality(discount);
			cartService.clear();
			cartService.clearDiscount();
			model.addAttribute("cart", cartService);

			mailerService.queue(address.getUser().getEmail(), "Đặt Hàng Thành Công Tại Web Smart Library",
					"Kính chào " + address.getUser().getFullname() +",<br>"
							+ "Cảm ơn bạn đã mua hàng tại Web Smart Libary. Mã đơn hàng của bạn là " + code + "<br>"
							+ "Xin vui lòng click vào đường link http://localhost:8080/account/order/invoice/" + code + " để xem chi tiết hóa đơn.<br>"
							+ "<br><br>"
							+ "Xin chân thành cảm ơn đã sử dụng dịch vụ,<br>"
							+ "Web Smart Libary");

			return "redirect:/shop/cart/checkout/success";
		} else {
			// Tính tổng giá trị đơn hàng
			double totalAmount = 0;
			List<CartModel> listCartModel = cartService.getItems().stream().toList();
			for (CartModel cart : listCartModel) {
				Product product = cart.getProduct();
				totalAmount += product.getPrice() * cart.getQuality();
			}
			int totalAmountInVND = (int) (totalAmount * 100);

			String vnp_Version = "2.1.0";
			String vnp_Command = "pay";
			String orderType = "other";
			String bankCode = "NCB";

			String vnp_TxnRef = com.shop.ecommerce.config.VNPayConfig.getRandomNumber(8);
			String vnp_IpAddr = "127.0.0.1";

			String vnp_TmnCode = com.shop.ecommerce.config.VNPayConfig.vnp_TmnCode;

			Map<String, String> vnp_Params = new HashMap<>();
			vnp_Params.put("vnp_Version", vnp_Version);
			vnp_Params.put("vnp_Command", vnp_Command);
			vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
			vnp_Params.put("vnp_Amount", String.valueOf(totalAmountInVND));  // Sử dụng tổng tiền tính được
			vnp_Params.put("vnp_CurrCode", "VND");

			vnp_Params.put("vnp_BankCode", bankCode);
			vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
			vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
			vnp_Params.put("vnp_OrderType", orderType);

			vnp_Params.put("vnp_Locale", "vn");
			vnp_Params.put("vnp_ReturnUrl", com.shop.ecommerce.config.VNPayConfig.vnp_ReturnUrl);
			vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

			Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String vnp_CreateDate = formatter.format(cld.getTime());
			vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

			cld.add(Calendar.MINUTE, 15);
			String vnp_ExpireDate = formatter.format(cld.getTime());
			vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

			List fieldNames = new ArrayList(vnp_Params.keySet());
			Collections.sort(fieldNames);
			StringBuilder hashData = new StringBuilder();
			StringBuilder query = new StringBuilder();
			Iterator itr = fieldNames.iterator();
			while (itr.hasNext()) {
				String fieldName = (String) itr.next();
				String fieldValue = (String) vnp_Params.get(fieldName);
				if ((fieldValue != null) && (fieldValue.length() > 0)) {
					hashData.append(fieldName);
					hashData.append('=');
					hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
					query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
					query.append('=');
					query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
					if (itr.hasNext()) {
						query.append('&');
						hashData.append('&');
					}
				}
			}
			String queryUrl = query.toString();
			String vnp_SecureHash = com.shop.ecommerce.config.VNPayConfig.hmacSHA512(com.shop.ecommerce.config.VNPayConfig.secretKey, hashData.toString());
			queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
			String paymentUrl = com.shop.ecommerce.config.VNPayConfig.vnp_PayUrl + "?" + queryUrl;

			return "redirect:" + paymentUrl;
		}
	}

	@GetMapping("/shop/cart/checkout/getPaymentInfo")
	public String getInfo(@RequestParam("vnp_TransactionNo") String transactionNo,
						  @RequestParam("vnp_OrderInfo") String orderInfo,
						  @RequestParam("vnp_TransactionStatus") String transactionStatus, Model model) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		if(!Objects.equals(transactionNo, "0")) {
			Address address = addressService.getAddressById(Integer.parseInt(this.addressId));
			Discount discount = cartService.getDiscount();

			if(discount.getId() == 0) {
				discount = null;
			}

			int code;
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = formatter.format(date);

			while (true) {
				code = (int) Math.floor(((Math.random() * 899999) + 100000));
				List<Order> list = orderService.getOrderByName(String.valueOf(code));
				if (list.isEmpty()) {
					break;
				}
			}

			List<CartModel> listCartModel = new ArrayList<>(cartService.getItems());
			for(CartModel cart: listCartModel) {
				Order order = new Order();
				Product product = cart.getProduct();
				order.setCode(String.valueOf(code));
				order.setAddress(address);
				order.setDiscount(discount);
				order.setProduct(product);
				order.setQuality(cart.getQuality());
				order.setDate(strDate);
				order.setMethod(method);
				order.setStatus("0");
				order.setComment(comment);
				orderService.save(order);

				product.setQuality(product.getQuality() - cart.getQuality());
				productService.updateQuality(product);
			}

			discountService.updateQuality(discount);
			cartService.clear();
			cartService.clearDiscount();
			model.addAttribute("cart", cartService);

			mailerService.queue(address.getUser().getEmail(), "Đặt Hàng Thành Công Tại Web Smart Library",
					"Kính chào " + address.getUser().getFullname() +",<br>"
							+ "Cảm ơn bạn đã mua hàng tại Web Smart Libary. Mã đơn hàng của bạn là " + code + "<br>"
							+ "Xin vui lòng click vào đường link http://localhost:8080/account/order/invoice/" + code + " để xem chi tiết hóa đơn.<br>"
							+ "<br><br>"
							+ "Xin chân thành cảm ơn đã sử dụng dịch vụ,<br>"
							+ "Web Smart Libary");
		}

		return "redirect:/shop/cart/checkout/success";
	}



	@GetMapping("/shop/cart/checkout/success")
	public String success(Model model) {
		return Constants.USER_DISPLAY_CHECKOUT_SUCCESS;
	}
	
	@ModelAttribute("total")
	public int tolal() {
		List<CartModel> list = new ArrayList<>(cartService.getItems());
		int total = 0;
		for(CartModel i: list) {
			total = total + i.getProduct().getPrice() * i.getQuality();
		}
		return total;
	}

	@ModelAttribute("listAddress")
	public List<Address> getListAddress(Model model) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = ((UserDetails) principal).getUsername();
		return addressService.findListAddressByEmail(username);
	}
}
