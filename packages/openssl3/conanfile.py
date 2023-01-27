from conans import ConanFile


class Openssl3Conan(ConanFile):
    def requirements(self):
        self.requires("openssl/3.0.7")

    def imports(self):
        self.copy("*.*", dst="lib", src="lib")
        # self.copy("libssl.a", dst="lib", src="lib")
        # self.copy("libcrypto.a", dst="lib", src="lib")
        # self.copy("*", dst="include", src="include")
