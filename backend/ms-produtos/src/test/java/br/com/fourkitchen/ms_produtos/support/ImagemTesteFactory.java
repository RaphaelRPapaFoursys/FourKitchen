package br.com.fourkitchen.ms_produtos.support;

import br.com.fourkitchen.ms_produtos.validation.ImagemBase64Utils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

public final class ImagemTesteFactory {

    private static final byte[] PNG_SIGNATURE = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private ImagemTesteFactory() {
    }

    public static byte[] criarPng(int largura, int altura) {
        return criarImagem(largura, altura, "png");
    }

    public static byte[] criarJpeg(int largura, int altura) {
        return criarImagem(largura, altura, "jpg");
    }

    public static String criarPngBase64(int largura, int altura) {
        return paraBase64(criarPng(largura, altura));
    }

    public static String criarJpegBase64(int largura, int altura) {
        return paraBase64(criarJpeg(largura, altura));
    }

    public static String criarDataUrlPng(int largura, int altura) {
        return "data:image/png;base64," + criarPngBase64(largura, altura);
    }

    public static String criarPngAcimaDoTamanhoMaximoBase64() {
        byte[] bytes = new byte[ImagemBase64Utils.TAMANHO_MAXIMO_BYTES + 1];
        System.arraycopy(PNG_SIGNATURE, 0, bytes, 0, PNG_SIGNATURE.length);
        return paraBase64(bytes);
    }

    public static String criarBase64FormatoNaoPermitido() {
        return paraBase64(Arrays.copyOf("arquivo".getBytes(), 20));
    }

    public static String paraBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] criarImagem(int largura, int altura, String formato) {
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = imagem.createGraphics();

        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, largura, altura);
            graphics.setColor(Color.ORANGE);
            graphics.fillRect(0, 0, Math.max(1, largura / 2), Math.max(1, altura / 2));
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(imagem, formato, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel gerar imagem para teste.", e);
        }
    }
}
