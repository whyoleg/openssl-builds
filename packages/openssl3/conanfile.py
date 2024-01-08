from os.path import join

from conan import ConanFile
from conan.tools.files import copy


class Openssl3Conan(ConanFile):
    options = {
        "shared": [True, False],
    }

    def requirements(self):
        self.requires("openssl/" + str(self.version))

    def generate(self):
        dep = self.dependencies["openssl"]
        includedir = dep.cpp_info.includedirs[0]
        libdir = dep.cpp_info.libdirs[0]
        bindir = dep.cpp_info.bindirs[0]

        copy(self, "*.h", includedir, join(self.build_folder, "include"))

        for libName in ["libcrypto", "libssl"]:
            for ext in [
                # mingw dynamic
                "dll.a",
                # windows dynamic
                "lib",
                # macos dynamic
                "dylib", "3.dylib",
                # linux dynamic
                "so.3", "so",
                # macos/linux/mingw static
                "a"
            ]:
                copy(self, libName + "." + ext, libdir, join(self.build_folder, "lib"))

        # windows only
        for binName in ["libcrypto-3-x64", "libssl-3-x64"]:
            for ext in ["dll"]:
                copy(self, binName + "." + ext, bindir, join(self.build_folder, "bin"))
