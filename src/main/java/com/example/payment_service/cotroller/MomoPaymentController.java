package com.example.payment_service.cotroller;

import com.example.payment_service.model.MomoPaymentRequest;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/momo-payment")
public class MomoPaymentController {

    private static final String ACCESS_KEY = "F8BBA842ECF85";
    private static final String SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    private static final String PARTNER_CODE = "MOMO";
    private static final String REDIRECT_URL = "http://localhost:5175/user/sales";
    private static final String IPN_URL = "http://localhost:5175/user/sales";

    @PostMapping()
    public ResponseEntity<?> createMomoPayment(@RequestBody MomoPaymentRequest request) {
        try {
            String orderId = PARTNER_CODE + System.currentTimeMillis();
            String requestId = orderId;
            String orderInfo = request.getOrderInfo();
            long amount = request.getAmount();

            String rawSignature = "accessKey=" + ACCESS_KEY +
                    "&amount=" + amount +
                    "&extraData=" +
                    "&ipnUrl=" + IPN_URL +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + PARTNER_CODE +
                    "&redirectUrl=" + REDIRECT_URL +
                    "&requestId=" + requestId +
                    "&requestType=captureWallet";

            String signature = hmacSHA256(rawSignature, SECRET_KEY);

            Map<String, Object> body = new HashMap<>();
            body.put("partnerCode", PARTNER_CODE);
            body.put("partnerName", "Test");
            body.put("storeId", "MomoTestStore");
            body.put("requestId", requestId);
            body.put("amount", String.valueOf(amount));
            body.put("orderId", orderId);
            body.put("orderInfo", orderInfo);
            body.put("redirectUrl", REDIRECT_URL);
            body.put("ipnUrl", IPN_URL);
            body.put("lang", "vi");
            body.put("requestType", "captureWallet");
            body.put("autoCapture", true);
            body.put("extraData", "");
            body.put("signature", signature);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, headers);

            String momoEndpoint = "https://test-payment.momo.vn/v2/gateway/api/create";
            ResponseEntity<Map> momoResponse = restTemplate.postForEntity(URI.create(momoEndpoint), httpEntity, Map.class);

            if (momoResponse.getStatusCode().is2xxSuccessful()) {Map<String, Object> responseBody = momoResponse.getBody();
                String payUrl = (String) responseBody.get("payUrl");
                return ResponseEntity.ok(Collections.singletonMap("payUrl", payUrl));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Failed to create MoMo payment");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }

    private String hmacSHA256(String data, String key) throws Exception {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        hmacSha256.init(secretKey);
        byte[] hash = hmacSha256.doFinal(data.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}