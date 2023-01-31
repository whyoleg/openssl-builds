from conans import ConanFile


class Openssl3Conan(ConanFile):
    def requirements(self):
        self.requires("openssl/3.0.7")

    def imports(self):
        self.copy("libcrypto.*", dst="lib", src="lib")
        self.copy("openssl/*", dst="include", src="include")
