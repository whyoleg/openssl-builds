from conans import ConanFile


class Openssl3Conan(ConanFile):
    def requirements(self):
        self.requires("openssl/3.0.8")

    def imports(self):
        self.copy("libcrypto.dll.a", dst="lib/dynamic", src="lib")
        self.copy("libcrypto.3.dylib", dst="lib/dynamic", src="lib")
        self.copy("libcrypto.dylib", dst="lib/dynamic", src="lib")
        self.copy("libcrypto.so.3", dst="lib/dynamic", src="lib")
        self.copy("libcrypto.so", dst="lib/dynamic", src="lib")

        self.copy("libcrypto.a", dst="lib/static", src="lib")

        self.copy("libssl.dll.a", dst="lib/dynamic", src="lib")
        self.copy("libssl.3.dylib", dst="lib/dynamic", src="lib")
        self.copy("libssl.dylib", dst="lib/dynamic", src="lib")
        self.copy("libssl.so.3", dst="lib/dynamic", src="lib")
        self.copy("libssl.so", dst="lib/dynamic", src="lib")

        self.copy("libssl.a", dst="lib/static", src="lib")

        self.copy("openssl/*", dst="include", src="include")
