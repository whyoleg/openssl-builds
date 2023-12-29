from os.path import join

from conan import ConanFile
from conan.tools.files import copy


class Openssl3Conan(ConanFile):
    def requirements(self):
        self.requires("openssl/" + str(self.version))

    def generate(self):
        for dep in [self.dependencies["openssl"], self.dependencies["zlib"]]:
            includedir = dep.cpp_info.includedirs[0]
            libdir = dep.cpp_info.libdirs[0]
            copy(self, "*.h", includedir, join(self.build_folder, "include"))
            copy(self, "*.dll", libdir, join(self.build_folder, "lib"))
            copy(self, "*.lib", libdir, join(self.build_folder, "lib"))
            copy(self, "*.a", libdir, join(self.build_folder, "lib"))
            copy(self, "*.dylib", libdir, join(self.build_folder, "lib"))
            copy(self, "*.so", libdir, join(self.build_folder, "lib"))
            copy(self, "*.so.3", libdir, join(self.build_folder, "lib"))
