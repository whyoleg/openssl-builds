from os.path import join

from conan import ConanFile
from conan.tools.files import copy


class Openssl3Conan(ConanFile):
    def requirements(self):
        self.requires("openssl/3.1.1")

    def generate(self):
        for dep in self.dependencies.values():
            copy(self, "*.h", dep.cpp_info.includedirs[0], join(self.build_folder, "include"))
            copy(self, "*.dll", dep.cpp_info.libdirs[0], join(self.build_folder, "lib"))
            copy(self, "*.lib", dep.cpp_info.libdirs[0], join(self.build_folder, "lib"))
            copy(self, "*.a", dep.cpp_info.libdirs[0], join(self.build_folder, "lib"))
            copy(self, "*.dylib", dep.cpp_info.libdirs[0], join(self.build_folder, "lib"))
            copy(self, "*.so", dep.cpp_info.libdirs[0], join(self.build_folder, "lib"))
            copy(self, "*.so.3", dep.cpp_info.libdirs[0], join(self.build_folder, "lib"))

    # def imports(self):
    #     # headers
    #     self.copy("*", dst="include", src="include")
    #     # Windows libraries
    #     self.copy("*.dll", dst="lib", src="bin")
    #     self.copy("*.lib", dst="lib", src="lib")
    #     # static libraries ( + dynamic mingw) libraries
    #     self.copy("*.a", dst="lib", src="lib")
    #     # macos dynamic libraries
    #     self.copy("*.dylib", dst="lib", src="lib")
    #     # linux/android dynamic libraries
    #     self.copy("*.so", dst="lib", src="lib")
    #     self.copy("*.so.3", dst="lib", src="lib")
