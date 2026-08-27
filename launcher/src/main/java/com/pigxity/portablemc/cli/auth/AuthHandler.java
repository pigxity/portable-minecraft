package com.pigxity.portablemc.cli.auth;

import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.java.JavaAuthManager;
import net.raphimc.minecraftauth.java.model.MinecraftProfile;
import net.raphimc.minecraftauth.java.model.MinecraftToken;
import net.raphimc.minecraftauth.msa.model.MsaDeviceCode;
import net.raphimc.minecraftauth.msa.service.impl.DeviceCodeMsaAuthService;
import com.pigxity.portablemc.shared.JsonFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class AuthHandler {
    private final HttpClient httpClient;

    public AuthHandler() {
        this.httpClient = MinecraftAuth.createHttpClient();
    }

    public AuthDetails get(Path jsonFile) throws Exception {
        JavaAuthManager auth = Files.exists(jsonFile)
                ? load(jsonFile)
                : login();

        MinecraftProfile profile = auth.getMinecraftProfile().getUpToDate();
        MinecraftToken token = auth.getMinecraftToken().getUpToDate();

        save(jsonFile, auth);

        return new AuthDetails(
                profile.getName(),
                token.getToken(),
                profile.getId().toString());
    }

    private JavaAuthManager login() throws Exception {
        return JavaAuthManager.create(httpClient)
                .login(DeviceCodeMsaAuthService::new, (Consumer<MsaDeviceCode>) deviceCode
                        -> System.out.println("Go to " + deviceCode.getDirectVerificationUri()));
    }

    private JavaAuthManager load(Path file) throws IOException {
        return JavaAuthManager.fromJson(httpClient, JsonFiles.readObject(file));
    }

    private void save(Path file, JavaAuthManager auth) throws IOException {
        JsonFiles.write(file, JavaAuthManager.toJson(auth));
    }
}
