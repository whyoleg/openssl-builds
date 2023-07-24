from conan import ConanFile


class Openssl3Conan(ConanFile):
    def requirements(self):
        self.requires("openssl/3.0.8")

    def imports(self):
        # headers
        self.copy("*", dst="include", src="include")
        # Windows libraries
        self.copy("*.dll", dst="lib", src="bin")
        self.copy("*.lib", dst="lib", src="lib")
        # static libraries ( + dynamic mingw) libraries
        self.copy("*.a", dst="lib", src="lib")
        # macos dynamic libraries
        self.copy("*.dylib", dst="lib", src="lib")
        # linux/android dynamic libraries
        self.copy("*.so", dst="lib", src="lib")
        self.copy("*.so.3", dst="lib", src="lib")
