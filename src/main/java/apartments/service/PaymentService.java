package apartments.service;

import apartments.constant.VNPayParams;
import apartments.dto.request.payment.InitPaymentRequest;
import apartments.dto.request.payment.InitPaymentResponse;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    private static final int DEFAULT_MULTIPLIER = 100;
    private static final String VERSION = "2.1.0";
    private static final String COMMAND = "pay";
    private static final String ORDER_TYPE = "other";
    @NonFinal
    @Value("${spring.payment.vnpay.tmn-code}")
    protected String tmnCode;

    @NonFinal
    @Value("${spring.payment.vnpay.return-url}")
    protected String returnUrlFormat;

    @NonFinal
    @Value("${spring.payment.vnpay.init-url}")
    protected String initPaymentPrefixUrl;

    @NonFinal
    @Value("${spring.payment.vnpay.secret-key}")
    protected String secretKey;

    @NonFinal
    @Value("${spring.payment.vnpay.timeout-minutes}")
    protected int paymentTimeout;

    final Mac mac;

    public PaymentService() throws NoSuchAlgorithmException, InvalidKeyException {
        mac = Mac.getInstance("HmacSHA512");
    }

    @PostConstruct
    void initMac() throws InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA512");
        mac.init(secretKeySpec);
    }

    public InitPaymentResponse init(InitPaymentRequest request) {
        var amount = request.getAmount() * DEFAULT_MULTIPLIER;
        var txnRef = request.getTxnRef();
        var returnUrl = String.format(returnUrlFormat, txnRef);

        var vnCalendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        var createdDate = formatDate(vnCalendar);
        vnCalendar.add(Calendar.MINUTE, paymentTimeout);
        var expiredDate = formatDate(vnCalendar);

        Map<String, String> params = new HashMap<>();
        params.put(VNPayParams.VERSION, VERSION);
        params.put(VNPayParams.COMMAND, COMMAND);
        params.put(VNPayParams.TMN_CODE, tmnCode);
        params.put(VNPayParams.AMOUNT, String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");

        params.put(VNPayParams.TXN_REF, txnRef);
        params.put(VNPayParams.RETURN_URL, returnUrl);
        params.put(VNPayParams.CREATED_DATE, createdDate);
        params.put(VNPayParams.EXPIRE_DATE, expiredDate);
        params.put(VNPayParams.IP_ADDRESS, request.getIpAddress());
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", String.format("Thanh toan don dat phong %s", txnRef));
        params.put(VNPayParams.ORDER_TYPE, ORDER_TYPE);

        String url = buildInitPaymentUrl(params);
        log.debug("[request_id={}] Init payment url: {}", request.getRequestId(), url);
        log.info("VNPay Request Params: {}", params);
        return InitPaymentResponse.builder().vnpUrl(url).build();
    }

    public boolean verifyIpn(Map<String, String> params) {
        var reqSecureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        var fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        var hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            var key = fieldNames.get(i);
            var value = params.get(key);
            if (value != null && !value.isEmpty()) {
                hashData.append(key).append("=").append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (i < fieldNames.size() - 1) hashData.append("&");
            }
        }

        String secureHash = sign(hashData.toString());
        return secureHash.equals(reqSecureHash);
    }

    private String formatDate(Calendar cal) {
        return String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", cal);
    }

    @SneakyThrows
    private String buildInitPaymentUrl(Map<String, String> params) {
        var fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        var hashData = new StringBuilder();
        var query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            var key = fieldNames.get(i);
            var value = params.get(key);
            if (value != null && !value.isEmpty()) {
                hashData.append(key).append("=").append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(key, StandardCharsets.US_ASCII))
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (i < fieldNames.size() - 1) {
                    hashData.append("&");
                    query.append("&");
                }
            }
        }

        String secureHash = sign(hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);
        return initPaymentPrefixUrl + "?" + query;
    }

    private String sign(String data) {
        try {
            return bytesToHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("VNPAY signing failed", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String getIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            var remoteAddr = request.getRemoteAddr();
            if (remoteAddr == null) {
                remoteAddr = "127.0.0.1";   // TODO: the ip of this BE app
            }

            return remoteAddr;
        }

        return xForwardedForHeader.split(",")[0].trim();
    }
}
