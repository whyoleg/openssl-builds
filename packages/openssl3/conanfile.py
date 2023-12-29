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
        libs = ["libcrypto", "libssl"]
        dep = self.dependencies["openssl"]
        includedir = dep.cpp_info.includedirs[0]
        libdir = dep.cpp_info.libdirs[0]

        # get headers from static build, to be consistent (TBD if it's a good solution)
        if not self.options.shared:
            copy(self, "*.h", includedir, join(self.build_folder, "include"))

            for lib in libs:
                copy(self, lib + ".a", libdir, join(self.build_folder, "staticLib"))
        else:
            for lib in libs:
                for ext in ["dll.a", "3.dylib", "dylib", "so.3", "so"]:
                    copy(self, lib + "." + ext, libdir, join(self.build_folder, "dynamicLib"))
