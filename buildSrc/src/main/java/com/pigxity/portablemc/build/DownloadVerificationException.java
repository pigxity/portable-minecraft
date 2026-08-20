package com.pigxity.portablemc.build;

import java.io.IOException;

final class DownloadVerificationException extends IOException {
    DownloadVerificationException(String message) {
        super(message);
    }
}
