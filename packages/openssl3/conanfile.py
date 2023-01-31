from conans import ConanFile


class Openssl3Conan(ConanFile):
    def requirements(self):
        self.requires("openssl/3.0.7")

    def imports(self):
        # static binary
        self.copy("libcrypto.a", dst="lib", src="lib")
        # windows dynamic binary
        self.copy("libcrypto.dll.a", dst="lib", src="lib")
        # linux dynamic binary
        self.copy("libcrypto.so", dst="lib", src="lib")
        # macos dynamic binary
        self.copy("libcrypto.dylib", dst="lib", src="lib")
        # headers
        self.copy("openssl/*", dst="include", src="include")
