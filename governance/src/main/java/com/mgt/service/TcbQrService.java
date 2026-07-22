package com.mgt.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class TcbQrService {

    public byte[] generateQr(String cardNo, int sizePx) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(
                cardNo,
                BarcodeFormat.QR_CODE,
                sizePx, sizePx,
                Map.of(EncodeHintType.MARGIN, 1)
            );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | java.io.IOException e) {
            throw new RuntimeException("QR generation failed for card: " + cardNo, e);
        }
    }
}
