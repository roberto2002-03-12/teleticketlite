package com.app.teleticket.qr.service;

import com.app.teleticket.qr.dto.QrPayload;
import com.app.teleticket.qr.dto.QrUploadResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.app.teleticket.qr.exception.QrException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@ApplicationScoped
public class QrCodeServiceImpl implements QrCodeService {

    private static final int QR_SIZE = 300;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public byte[] generate(Integer userId, Integer eventId) {
        String content = String.format("{\"userId\":%d,\"eventId\":%d}", userId, eventId);
        try {
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.MARGIN, 1);
            var matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (WriterException | IOException e) {
            throw new QrException(500, "QR code generation failed: " + e.getMessage());
        }
    }

    @Override
    public QrPayload decode(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length == 0) {
            throw new QrException(400, "Empty QR image");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new QrException(400, "Invalid QR image");
            }
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Map<DecodeHintType, Object> hints = Map.of(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            Result result = new MultiFormatReader().decode(bitmap, hints);
            JsonNode node = objectMapper.readTree(result.getText());
            if (node.get("userId") == null || node.get("eventId") == null) {
                throw new QrException(400, "Invalid QR payload");
            }
            return new QrPayload(node.get("userId").asInt(), node.get("eventId").asInt());
        } catch (NotFoundException e) {
            throw new QrException(400, "No QR code found in the image");
        } catch (QrException e) {
            throw e;
        } catch (IOException e) {
            throw new QrException(400, "Invalid QR image: " + e.getMessage());
        }
    }
}