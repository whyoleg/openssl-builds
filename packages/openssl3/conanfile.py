from conans import ConanFile


class Openssl3Conan(ConanFile):
    def requirements(self):
        self.requires("openssl/3.0.8")

    def imports(self):
        self.copy("*", dst="lib", src="lib")
        self.copy("openssl/*", dst="include", src="include")
