package com.pigxity.portablemc.build.download;

import java.io.IOException;

final class DownloadVerificationException extends IOException {
    DownloadVerificationException(String message) {
        super(message);
    }
}
