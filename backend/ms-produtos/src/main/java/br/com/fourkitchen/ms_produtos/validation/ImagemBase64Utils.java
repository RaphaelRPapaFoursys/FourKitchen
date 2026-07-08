package br.com.fourkitchen.ms_produtos.validation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public final class ImagemBase64Utils {

    public static final int TAMANHO_MAXIMO_BYTES = 1024 * 1024;
    public static final int LARGURA_MAXIMA = 1200;
    public static final int ALTURA_MAXIMA = 900;

    private static final String DATA_URL_BASE64_MARKER = ";base64,";
    private static final byte[] PNG_SIGNATURE = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private ImagemBase64Utils() {
    }

    public static byte[] decodificarEValidar(String imagem) {
        byte[] bytes = decodificar(imagem);

        if (bytes == null) {
            return null;
        }

        validarImagem(bytes);
        return bytes;
    }

    public static byte[] decodificar(String imagem) {
        if (imagem == null || imagem.isBlank()) {
            return null;
        }

        return Base64.getDecoder().decode(normalizar(imagem));
    }

    public static void validarImagem(byte[] bytes) {
        if (bytes.length > TAMANHO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("Imagem acima do tamanho maximo permitido.");
        }

        if (!isJpeg(bytes) && !isPng(bytes)) {
            throw new IllegalArgumentException("Formato da imagem nao permitido.");
        }

        BufferedImage imagem = lerImagem(bytes);
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();

        if (largura > LARGURA_MAXIMA || altura > ALTURA_MAXIMA) {
            throw new IllegalArgumentException("Imagem acima das dimensoes maximas permitidas.");
        }

        if (!isProporcaoQuatroPorTres(largura, altura)) {
            throw new IllegalArgumentException("Imagem deve estar na proporcao 4:3.");
        }
    }

    private static String normalizar(String imagem) {
        String imagemNormalizada = imagem.trim();

        if (imagemNormalizada.startsWith("data:")) {
            int base64Index = imagemNormalizada.indexOf(DATA_URL_BASE64_MARKER);

            if (base64Index < 0) {
                throw new IllegalArgumentException("Data URL sem conteudo Base64.");
            }

            imagemNormalizada = imagemNormalizada.substring(base64Index + DATA_URL_BASE64_MARKER.length());
        }

        return imagemNormalizada.replaceAll("\\s+", "");
    }

    private static boolean isJpeg(byte[] bytes) {
        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private static boolean isPng(byte[] bytes) {
        if (bytes.length < PNG_SIGNATURE.length) {
            return false;
        }

        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if (bytes[i] != PNG_SIGNATURE[i]) {
                return false;
            }
        }

        return true;
    }

    private static BufferedImage lerImagem(byte[] bytes) {
        try {
            BufferedImage imagem = ImageIO.read(new ByteArrayInputStream(bytes));

            if (imagem == null) {
                throw new IllegalArgumentException("Imagem nao pode ser lida.");
            }

            return imagem;
        } catch (IOException e) {
            throw new IllegalArgumentException("Imagem nao pode ser lida.", e);
        }
    }

    private static boolean isProporcaoQuatroPorTres(int largura, int altura) {
        return (long) largura * 3 == (long) altura * 4;
    }
}
