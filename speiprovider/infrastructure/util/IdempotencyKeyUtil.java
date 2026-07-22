package com.netpay.speiprovider.infrastructure.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public final class IdempotencyKeyUtil {

    private IdempotencyKeyUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    /**
     * Genera la clave de idempotencia final basada en la especificación.
     */
    public static String generate(UUID namespace, String clientId, String method, Object requestBody) {
        try {
            // 1. Convertir el objeto a JSON String Canónico (Ordenado alfabéticamente)
            String canonicalJson = toCanonicalJson(requestBody);

            // 2. Calcular el SHA-256 del JSON string
            String bodyHash = calculateSha256(canonicalJson);

            // 3. Construir la cadena 'name'
            String name = clientId + method + bodyHash;

            // 4. Generar el UUIDv5 final
            return generateUUIDv5(namespace, name).toString();
        } catch (Exception e) {
            throw new IllegalStateException("Error al calcular el Idempotency-Key determinista", e);
        }
    }

    private static String toCanonicalJson(Object obj) throws JsonProcessingException {
        JsonNode tree = MAPPER.valueToTree(obj);
        JsonNode sortedTree = sortJsonNode(tree);
        return MAPPER.writeValueAsString(sortedTree);
    }

    private static JsonNode sortJsonNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sortedNode = MAPPER.createObjectNode();
            List<String> fieldNames = new ArrayList<>();
            node.fieldNames().forEachRemaining(fieldNames::add);
            Collections.sort(fieldNames);

            for (String fieldName : fieldNames) {
                sortedNode.set(fieldName, sortJsonNode(node.get(fieldName)));
            }
            return sortedNode;
        } else if (node.isArray()) {
            ArrayNode sortedArray = MAPPER.createArrayNode();
            for (JsonNode item : node) {
                sortedArray.add(sortJsonNode(item));
            }
            return sortedArray;
        }
        return node;
    }

    private static String calculateSha256(String text) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static UUID generateUUIDv5(UUID namespace, String name) throws NoSuchAlgorithmException {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] nsBytes = new byte[16];

        ByteBuffer.wrap(nsBytes)
                .putLong(namespace.getMostSignificantBits())
                .putLong(namespace.getLeastSignificantBits());

        byte[] data = new byte[nsBytes.length + nameBytes.length];
        System.arraycopy(nsBytes, 0, data, 0, nsBytes.length);
        System.arraycopy(nameBytes, 0, data, nsBytes.length, nameBytes.length);

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(data);

        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) msb = (msb << 8) | (hash[i] & 0xff);
        for (int i = 8; i < 16; i++) lsb = (lsb << 8) | (hash[i] & 0xff);

        // Forzar Versión 5 (bits 12-15 del tiempo alto a 0101)
        msb &= 0xFFFFFFFFFFFF0FFFL;
        msb |= 0x0000000000005000L;
        // Forzar Variante IETF (bits 6-7 de clock_seq a 10)
        lsb &= 0x3FFFFFFFFFFFFFFFL;
        lsb |= 0x8000000000000000L;

        return new UUID(msb, lsb);
    }
}