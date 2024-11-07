from os.path import join

from conan import ConanFile
from conan.tools.files import copy


class LibcurlConan(ConanFile):
    options = {
        "shared": [True, False],
    }

    def requirements(self):
        self.requires("libcurl/" + str(self.version))

    def generate(self):
        for _, dep in self.dependencies.host.items():
            includedir = dep.cpp_info.includedirs
            libdir = dep.cpp_info.libdirs
            bindir = dep.cpp_info.bindirs
            for ext, sources, destination in [
                # headers
                ("*.h", includedir, "include"),
                # mingw dynamic
                ("*.dll.a", libdir, "lib"),
                # windows dynamic
                ("*.lib", libdir, "lib"),
                # macos dynamic
                ("*.dylib", libdir, "lib"),
                ("*.*.dylib", libdir, "lib"),
                # linux dynamic
                ("*.so", libdir, "lib"),
                ("*.so.*", libdir, "lib"),
                # any static
                ("*.a", libdir, "lib"),
                # windows only binaries
                ("*.dll", bindir, "bin")
            ]:
                for source in sources:
                    copy(self, ext, source, join(self.build_folder, destination))
